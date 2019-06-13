package com.github.oowekyala.jjtx.postprocessor

import com.github.oowekyala.ijcc.lang.model.addParserPackage
import com.github.oowekyala.ijcc.lang.model.parserQualifiedName
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.reporting.reportFatal
import spoon.OutputType
import spoon.processing.AbstractProcessor
import spoon.processing.Processor
import spoon.reflect.code.CtBlock
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtLiteral
import spoon.reflect.code.CtTry
import spoon.reflect.declaration.CtType
import spoon.reflect.factory.TypeFactory
import spoon.reflect.reference.CtTypeReference
import spoon.reflect.visitor.filter.TypeFilter
import java.nio.file.Path
import spoon.Launcher as SpoonLauncher
import spoon.reflect.code.CtStatement as CtStatement


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

    val realToken = ctx.tokenClass

    val realTokenRef = TypeFactory().createReference<Any>(realToken.qualifiedName)

    val jjToken = ctx.jjtxOptsModel.addParserPackage("Token")
    val renamer: Processor<CtTypeReference<*>> = object : AbstractProcessor<CtTypeReference<*>>() {
        override fun process(element: CtTypeReference<*>) {
            if (element.qualifiedName == jjToken) {
                element.replace(realTokenRef.clone())
            }
        }
    }

    val inliner = object : AbstractProcessor<CtIf>() {
        override fun process(element: CtIf) {
            val condition = element.condition as? CtLiteral<Boolean> ?: return

            if (condition.value == true) {
                element.replace(element.getThenStatement<CtStatement>().clone())
            } else if (condition.value == false) {
                element.replace(element.getElseStatement<CtStatement>().clone())
            }
        }
    }

    val blockFlattener = object : AbstractProcessor<CtBlock<*>>() {
        override fun process(element: CtBlock<*>) {

            if (element.statements.size == 1 && element.parent is CtBlock<*>) {
                // element.setImplicit<CtBlock<*>>(true)
                element.replace(element.statements.first().clone())
            }
        }
    }

    spoonModel.allTypes.forEach {
        it.getElements(TypeFilter(CtTypeReference::class.java))?.forEach {
            renamer.process(it)
        }
        it.getElements(TypeFilter(CtIf::class.java))?.forEach {
            inliner.process(it)
        }

        it.getElements(TypeFilter(CtBlock::class.java))?.forEach {
            blockFlattener.process(it)
        }

        spoon.createOutputWriter().createJavaFile(it)
    }





}





