package com.github.oowekyala.jjtx.postprocessor

import com.github.oowekyala.ijcc.lang.model.addParserPackage
import com.github.oowekyala.jjtx.JjtxContext
import spoon.OutputType
import spoon.processing.AbstractProcessor
import spoon.processing.Processor
import spoon.reflect.code.CtBlock
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtLiteral
import spoon.reflect.code.CtStatement
import spoon.reflect.declaration.CtElement
import spoon.reflect.factory.TypeFactory
import spoon.reflect.reference.CtTypeReference
import spoon.reflect.visitor.filter.TypeFilter
import java.nio.file.Path
import spoon.Launcher as SpoonLauncher


fun mapJavaccOutput(ctx: JjtxContext, jccOutput: Path, realOutput: Path, otherSources: List<Path>) {

    // javacc was generated into [jccOutput]
    // existing classes are in [realOutput] and [otherSources]
    // we know that the desired token class is [ctx.tokenClass]


    val (spoon, spoonModel) = SpoonLauncher().run {
        addInputResource(jccOutput.toString())
        // addInputResource(realOutput.toString())
        environment.isAutoImports = true

        environment.outputType = OutputType.COMPILATION_UNITS

        // FIXME bug in sniper with loop
        //   val foo = SpoonLauncher.parseClass("class Foo { private int[] arr = new int[3]; { for (int i = 0; i<3;i++) arr[i]  = 5;}")
        //           environment.setPrettyPrinterCreator {
        //               SniperJavaPrettyPrinter(environment)
        //           }

        environment.sourceOutputDirectory = realOutput.toFile()

        buildModel()
        Pair(this, model)
    }


    val jjToken = ctx.jjtxOptsModel.addParserPackage("Token")
    val renamer: Processor<CtTypeReference<*>> = TypeReferenceRenamer(jjToken, ctx.tokenClass.qualifiedName)


    val processors = listOf(
        // can't factorise that bc of the inline
        renamer.treeProcessor(),
        IfStmtConstantFolder.treeProcessor(),
        BlockUnwrapper.treeProcessor()
    )


    spoonModel.allTypes.forEach {
        processors.forEach { p -> p.process(it) }
        spoon.createOutputWriter().createJavaFile(it)
    }





}

inline fun <reified T : CtElement> Processor<T>.treeProcessor(): Processor<in CtElement> =
    object : AbstractProcessor<CtElement>() {
        override fun process(element: CtElement) {
            element.getElements(TypeFilter(T::class.java)).forEach {
                this@treeProcessor.process(it)
            }
        }
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

class TypeReferenceRenamer(private val sourceQname: String,
                           targetQname: String)
    : AbstractProcessor<CtTypeReference<*>>() {

    private val targetRef = TypeFactory().createReference<Any>(targetQname)!!


    override fun process(element: CtTypeReference<*>) {
        if (element.qualifiedName == sourceQname) {
            element.replace(targetRef.clone())
        }
    }
}





