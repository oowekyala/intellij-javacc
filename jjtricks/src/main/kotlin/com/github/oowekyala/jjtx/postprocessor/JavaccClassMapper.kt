package com.github.oowekyala.jjtx.postprocessor

import com.github.oowekyala.ijcc.lang.model.parserPackage
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.reporting.debug
import com.github.oowekyala.jjtx.templates.vbeans.ClassVBean
import com.github.oowekyala.jjtx.util.asQnamePath
import com.github.oowekyala.jjtx.util.joinTasks
import spoon.OutputType
import spoon.compiler.Environment
import spoon.processing.AbstractProcessor
import spoon.processing.Processor
import spoon.reflect.code.CtBlock
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtLiteral
import spoon.reflect.code.CtStatement
import spoon.reflect.declaration.CtElement
import spoon.reflect.declaration.CtPackage
import spoon.reflect.declaration.CtType
import spoon.reflect.factory.PackageFactory
import spoon.reflect.visitor.DefaultJavaPrettyPrinter
import spoon.reflect.visitor.filter.TypeFilter
import java.lang.reflect.ParameterizedType
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis
import spoon.Launcher as SpoonLauncher


fun mapJavaccOutput(ctx: JjtxContext,
                    jccOutput: Path,
                    realOutput: Path,
                    otherSources: List<Path>,
                    outputFilter: (String) -> Boolean) {

    // javacc was generated into [jccOutput]
    // existing classes are in [realOutput] and [otherSources]
    // we know that the desired token class is [ctx.tokenClass]


    val (spoonLauncher, spoonModel) = SpoonLauncher().run {
        otherSources.forEach {
            addInputResource(it.resolve(ctx.jjtxOptsModel.parserPackage.asQnamePath()).toString())
        }
        // addInputResource(realOutput.toString())
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

    // Only start writing after all processing is done
    val toWrite = mutableListOf<CtType<*>>()

    for (type in spoonModel.allTypes) {
        if (type.position.file.toPath().parent != jccOutput) {
            // ctx.messageCollector.debug("Skipping ${type.qualifiedName} because ${type.position.file} not in $jccOutput")
            continue
        }

        specialMappingRaw[type.qualifiedName]?.let { qn ->
            type.relocate(qn)
        }

        if (outputFilter(type.qualifiedName)) {
            val t = measureTimeMillis {
                composite(type)
            }
            ctx.messageCollector.debug("Processed ${type.qualifiedName} in $t ms, waiting for write access")
            toWrite += type
        }
    }

    toWrite.map { type ->

        CompletableFuture.runAsync {
            val to = measureTimeMillis {
                spoonLauncher.createOutputWriter().createJavaFile(type)
            }
            ctx.messageCollector.debug("Written ${type.qualifiedName} in $to ms")
        }
    }.joinTasks()

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


private typealias MyProcessor = (CtElement) -> Unit

fun composeProcessors(processors: List<Processor<*>>): MyProcessor {


    /**
     * Turns a processor of anything into a processor of [CtElement], that
     * targets only the original type of target. This requires [this] processor
     * to extend [AbstractProcessor] *directly*.
     */
    fun Processor<*>.treeProcessor(): Processor<in CtElement> {

        fun <T : CtElement> Processor<T>.treeProcessorCapture(): Processor<in CtElement> {
            val target: Class<T> =
                this.javaClass.genericSuperclass.let { it as ParameterizedType }.actualTypeArguments[0].let {
                    if (it is Class<*>) it
                    else (it as ParameterizedType).rawType
                } as Class<T>

            return object : AbstractProcessor<CtElement>() {
                override fun process(element: CtElement) {
                    element.getElements(TypeFilter(target)).forEach<T> {
                        this@treeProcessorCapture.process(it)
                    }
                }
            }
        }

        return treeProcessorCapture()
    }

    val treeProcessors = processors.map { it.treeProcessor() }

    return { root ->
        treeProcessors.forEach { it.process(root) }
    }
}


// FIXME sniper pretty printer might be the only way to make that fast for big files
//   - in fact no, since we're cleaning up some javacc goo a bit everywhere
//   only realistic way I see here would be to split the file into chunks and render
//   them in parallel... printer is not synchronized though, and this won't work on
//   non-parallel-capable systems
//   - this is shitty anyway... best use another framework maybe
class MyJPrettyPrinter(env: Environment) : DefaultJavaPrettyPrinter(env) {
    //
    //    private val printer = DefaultTokenWriter(PrinterHelper(env))
    //
    //    init {
    //        this.printerTokenWriter = printer
    //    }


}
