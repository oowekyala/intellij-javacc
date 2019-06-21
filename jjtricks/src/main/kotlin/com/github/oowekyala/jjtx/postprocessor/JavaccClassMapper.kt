package com.github.oowekyala.jjtx.postprocessor

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.templates.vbeans.ClassVBean
import com.github.oowekyala.jjtx.util.joinTasks
import spoon.OutputType
import spoon.compiler.Environment
import spoon.processing.AbstractProcessor
import spoon.processing.Processor
import spoon.reflect.code.*
import spoon.reflect.declaration.CtElement
import spoon.reflect.declaration.CtPackage
import spoon.reflect.declaration.CtType
import spoon.reflect.factory.PackageFactory
import spoon.reflect.visitor.DefaultJavaPrettyPrinter
import spoon.reflect.visitor.DefaultTokenWriter
import spoon.reflect.visitor.PrinterHelper
import spoon.support.sniper.SniperJavaPrettyPrinter
import java.lang.reflect.ParameterizedType
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis
import spoon.Launcher as SpoonLauncher


fun mapJavaccOutput(ctx: JjtxContext, jccOutput: Path, realOutput: Path, outputFilter: (String) -> Boolean) {

    // javacc was generated into [jccOutput]
    // existing classes are in [realOutput] and [otherSources]
    // we know that the desired token class is [ctx.tokenClass]


    val (spoonLauncher, spoonModel) = SpoonLauncher().run {
        addInputResource(jccOutput.toString())
        // addInputResource(realOutput.toString())
        environment.isAutoImports = true

        environment.outputType = OutputType.COMPILATION_UNITS

        // FIXME bug in sniper with loop
        //   val foo = SpoonLauncher.parseClass("class Foo { private int[] arr = new int[3]; { for (int i = 0; i<3;i++) arr[i]  = 5;}")
        //           environment.setPrettyPrinterCreator {
        //               SniperJavaPrettyPrinter(environment)
        //           }

        environment.setPrettyPrinterCreator {
            MyJPrettyPrinter(environment)
        }

        environment.sourceOutputDirectory = realOutput.toFile()

        buildModel()
        Pair(this, model)
    }


    val specials = listOf(
        SpecialTemplate.TOKEN,
        SpecialTemplate.TOKEN_MANAGER,
        SpecialTemplate.PARSE_EXCEPTION,
        SpecialTemplate.LEX_EXCEPTION,
        SpecialTemplate.CHAR_STREAM
    )
    val specialMapping = specials.associateBy { it.defaultLocation(ctx.jjtxOptsModel) }
        .mapValues { (_, it) -> it.actualLocation(ctx.jjtxOptsModel) }

    val specialMappingRaw = specialMapping.map { (from, to) -> Pair(from.qualifiedName, to) }.toMap()


    val processors: List<Processor<out CtElement>> =
        listOf(AssignmentSpreader)
            .plus(TypeReferenceRenamer(specialMapping))
            .plus(
                listOf(IfStmtConstantFolder, BlockUnwrapper)
            )

    val composite = composeProcessors(processors)

    val writeTasks = mutableListOf<CompletableFuture<Void>>()

    spoonModel.allTypes.forEach<CtType<*>> {

        specialMappingRaw[it.qualifiedName]?.let { qn ->
            it.relocate(qn)
        }


        if (outputFilter(it.qualifiedName)) {
            synchronized(System.out) {
                println("Processing ${it.qualifiedName}")
            }
            val t = measureTimeMillis {
                composite(it)
            }
            synchronized(System.out) {
                println("Done in $t ms")
            }
            writeTasks += CompletableFuture.runAsync {
                val to = measureTimeMillis {
                    spoonLauncher.createOutputWriter().createJavaFile(it)
                }
                synchronized(System.out) {
                    println("Written ${it.qualifiedName} in $to ms")
                }
            }
        }
    }

    writeTasks.joinTasks()


}


fun CtType<*>.relocate(actualLocation: ClassVBean) {

    `package`.removeType(this)

    val actualPack = PackageFactory(factory).getOrCreate(actualLocation.`package`)

    position.compilationUnit.packageDeclaration =
        PackageFactory(factory).createPackageDeclaration(actualPack.reference)

    if (simpleName != actualLocation.simpleName) {
        setSimpleName<CtType<*>>(actualLocation.simpleName)
    }

    actualPack.addType<CtPackage>(this)
}

object BlockUnwrapper : AbstractProcessor<CtBlock<*>>() {
    override fun process(element: CtBlock<*>) {

        if (element.statements.size == 1 && element.parent is CtBlock<*>) {
            // element.setImplicit<CtBlock<*>>(true)
            element.replace(element.statements.first().clone())
        }
    }
}

object IfStmtConstantFolder : AbstractProcessor<CtIf>() {
    override fun process(element: CtIf) {
        val condition = element.condition as? CtLiteral<Boolean> ?: return

        if (condition.value == true) {
            element.replace(element.getThenStatement<CtStatement>().clone())
        } else if (condition.value == false) {
            element.replace(element.getElseStatement<CtStatement>().clone())
        }
    }
}

// TODO this is probably better than composeProcessors
class CompositeProcessor(processors: List<Processor<*>>) : AbstractProcessor<CtElement>() {


    private val typeMap: Map<Class<*>, Processor<*>> = processors.associateBy { p ->
        p.javaClass.genericSuperclass.let { it as ParameterizedType }.actualTypeArguments[0].let {
            if (it is Class<*>) it
            else (it as ParameterizedType).rawType
        } as Class<*>
    }

    override fun process(element: CtElement) {

        typeMap.forEach { (t, p) ->
            if (t.isInstance(element))
                (p as Processor<CtElement>).process(element)
        }
    }
}

private typealias MyProcessor = (CtElement) -> Unit

fun composeProcessors(processors: List<Processor<*>>): MyProcessor = { root ->
    val typeSet = processors.map { p ->
        val target = p.javaClass.genericSuperclass.let { it as ParameterizedType }.actualTypeArguments[0].let {
            if (it is Class<*>) it
            else (it as ParameterizedType).rawType
        } as Class<*>

        Pair(target, p)
    }

    root.filterChildren<CtElement> { true }.forEach<CtElement> {
        for ((t, p) in typeSet) {
            if (t.isInstance(it)) (p as Processor<CtElement>).process(it)
        }
    }
}


fun <T : CtElement> noopProcessor(): Processor<T> = NoopProcessor as Processor<T>

private object NoopProcessor : AbstractProcessor<CtElement>() {
    override fun process(element: CtElement) {
        // do nothing
    }
}


class MyJPrettyPrinter(env: Environment) : DefaultJavaPrettyPrinter(env)
