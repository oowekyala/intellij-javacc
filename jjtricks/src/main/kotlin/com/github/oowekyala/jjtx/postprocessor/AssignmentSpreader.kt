package com.github.oowekyala.jjtx.postprocessor

import spoon.processing.AbstractProcessor
import spoon.reflect.code.*
import spoon.reflect.declaration.CtMethod
import spoon.reflect.declaration.CtParameter
import spoon.reflect.declaration.CtType
import spoon.reflect.declaration.ModifierKind
import spoon.reflect.reference.CtTypeReference
import java.util.*
import kotlin.math.absoluteValue

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
                EnumSet.of(
                    ModifierKind.PRIVATE,
                    ModifierKind.STATIC
                ),
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
