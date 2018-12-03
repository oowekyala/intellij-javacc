// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.insight.model.JccOptionType.RefinedOptionType.PACKAGE
import com.github.oowekyala.ijcc.insight.model.JccOptionType.RefinedOptionType.TYPE
import com.github.oowekyala.ijcc.lang.JavaccTypes.JCC_INTEGER_LITERAL
import com.github.oowekyala.ijcc.lang.JavaccTypes.JCC_STRING_LITERAL
import com.github.oowekyala.ijcc.lang.psi.JccBooleanLiteral
import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding
import com.github.oowekyala.ijcc.lang.psi.JccOptionValue
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassListReferenceProvider

class JccOptionValueImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccOptionValue {

    override val booleanLiteral: JccBooleanLiteral?
        get() = findChildByClass(JccBooleanLiteral::class.java)

    override val integerLiteral: PsiElement?
        get() = findChildByType(JCC_INTEGER_LITERAL)

    override val stringLiteral: PsiElement?
        get() = findChildByType<PsiElement>(JCC_STRING_LITERAL)?.let { StringRefWrapper(it) }


    fun accept(visitor: JccVisitor) {
        visitor.visitOptionValue(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    companion object {
        val classRefProvider = JavaClassListReferenceProvider()

        private class StringRefWrapper(psiElement: PsiElement) : PsiElement by psiElement {

            override fun getReference(): PsiReference? {
                val refs = references
                return if (refs.isEmpty()) null else refs[0]
            }

            override fun getReferences(): Array<PsiReference> {
                val type = parent.let { it as JccOptionBinding }.modelOption?.expectedType
                if (type == TYPE || type == PACKAGE) {
                    return classRefProvider.getReferencesByElement(this)
                }
                return emptyArray()
            }
        }
    }
}
