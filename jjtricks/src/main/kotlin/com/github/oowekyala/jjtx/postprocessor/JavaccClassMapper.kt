package com.github.oowekyala.jjtx.postprocessor

import com.github.oowekyala.jjtx.JjtxContext
import spoon.OutputType
import spoon.processing.AbstractProcessor
import spoon.processing.Processor
import spoon.reflect.code.*
import spoon.reflect.declaration.*
import spoon.reflect.factory.TypeFactory
import spoon.reflect.reference.CtTypeReference
import spoon.reflect.visitor.filter.TypeFilter
import java.lang.reflect.ParameterizedType
import java.nio.file.Path
import java.util.*
import kotlin.math.absoluteValue
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

    fun SpecialTemplate.templateRenamer(): Processor<CtTypeReference<*>> =
        ctx.jjtxOptsModel.let { opts ->
            TypeReferenceRenamer(
                sourceQname = defaultLocation(opts).qualifiedName,
                targetQname = actualLocation(opts).qualifiedName
            )
        }



    val processors: List<Processor<in CtElement>> = listOf(
        AssignmentSpreader,
        // TODO generalise to all special templates
        SpecialTemplate.TOKEN.templateRenamer(),
        IfStmtConstantFolder,
        BlockUnwrapper
    ).map { it.treeProcessor() }


    spoonModel.allTypes.forEach<CtType<*>> {
        processors.forEach { p -> p.process(it as CtElement) }
        spoon.createOutputWriter().createJavaFile(it)
    }


}

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
                    this@treeProcessor.process(it)
                }
            }
        }
    }

    return treeProcessorCapture()
}

object BlockUnwrapper : AbstractProcessor<CtBlock<*>>() {
    override fun process(element: CtBlock<*>) {

        if (element.statements.size == 1 && element.parent is CtBlock<*>) {
            // element.setImplicit<CtBlock<*>>(true)
            element.replace(element.statements.first().clone())
        }
    }
}

/**
 * Transforms assignments of a field that occur in an expression context
 * into method calls. E.g. for an assignment chain:
 *
 *      a.k = b.x = c;
 *      // becomes:
 *
 *      a.k = set$B$X$090(b, c);
 *
 *      X set$B$X$090(B lhs, X rhs) {
 *          lhs.x = rhs;
 *          return rhs;
 *      }
 *
 * This allows rewriting the field assignment lhs.x to a setter call later on,
 * without changing program semantics.
 */
object AssignmentSpreader : AbstractProcessor<CtAssignment<*, *>>() {
    override fun process(element: CtAssignment<*, *>) {
        val lhs = element.getAssigned()
        val rhs = element.getAssignment()
        if (element.getParent() is CtExpression<*> && lhs is CtFieldWrite<*>) {

            if (lhs.target.isImplicit) return

            // ok we have nested assignment
            val enclosing: CtType<Any> = element.getParent(CtType::class.java as Class<CtType<Any>>)

            val receiver: CtParameter<Any> = enclosing.factory.createParameter<Any>()
            receiver.setType<CtParameter<Any>>(lhs.type as CtTypeReference<Any>)
                .setSimpleName<CtParameter<Any>>("lhs")

            val value: CtParameter<Any> = enclosing.factory.createParameter<Any>()
            value.setType<CtParameter<Any>>(rhs.type as CtTypeReference<Any>)
                .setSimpleName<CtParameter<Any>>("rhs")

            val m: CtMethod<Any> = enclosing.factory.createMethod(
                enclosing,
                EnumSet.of(ModifierKind.PRIVATE, ModifierKind.STATIC),
                element.getType(),
                mangleSetterName(lhs),
                listOf<CtParameter<*>>(
                    receiver,
                    value
                ),
                emptySet()
            )

            val ass = m.factory.createAssignment<Any, Any>()

            ass.setAssigned<CtAssignment<Any, Any>>(m.factory.createCodeSnippetExpression<Any>(receiver.simpleName + "." + lhs.variable.simpleName))
            ass.setAssignment<CtAssignment<Any, Any>>(m.factory.createCodeSnippetExpression<Any>(value.simpleName))

            val ret = m.factory.createReturn<Any>()
            ret.setReturnedExpression<CtReturn<Any>>(m.factory.createCodeSnippetExpression<Any>(value.simpleName))

            m.setBody<CtMethod<*>>(m.factory.createBlock<Any>())

            m.body.addStatement<CtStatementList>(ass)
            m.body.addStatement<CtStatementList>(ret)

            enclosing.addMethod<Any, CtType<Any>>(m)


            val invocation = m.factory.createInvocation(
                m.factory.createTypeAccess(enclosing.reference),
                m.reference,
                lhs.target.clone(),
                rhs.clone()
            )

            invocation.target.setImplicit<CtTypeAccess<*>>(true)

            element.replace(invocation)

        }
    }

    private fun mangleSetterName(lhs: CtFieldWrite<*>) =
        "set\$" + lhs.target.type.simpleName +
            "\$" + lhs.variable.simpleName +
            // this ensures we don't overwrite a method with an unrelated type whose simple name collides
            "\$" + Integer.toHexString(lhs.variable.qualifiedName.hashCode().absoluteValue).substring(0, endIndex = 4)
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

/**
 * Renames references to a type.
 *
 * @param targetQname The target of the renaming
 */
class TypeReferenceRenamer private constructor(
    private val mySourceQname: String,
    targetQname: String
) : AbstractProcessor<CtTypeReference<*>>() {

    private val targetRef = TypeFactory().createReference<Any>(targetQname)!!


    override fun process(element: CtTypeReference<*>) {
        if (element.qualifiedName == mySourceQname) {
            element.replace(targetRef.clone())
        }
    }

    companion object {
        operator fun invoke(sourceQname: String, targetQname: String): Processor<CtTypeReference<*>> =
            if (sourceQname == targetQname) noopProcessor()
            else TypeReferenceRenamer(mySourceQname = sourceQname, targetQname = targetQname)
    }
}

fun <T : CtElement> noopProcessor(): Processor<T> = NoopProcessor as Processor<T>

private object NoopProcessor : AbstractProcessor<CtElement>() {
    override fun process(element: CtElement) {
        // do nothing
    }
}




