// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.insight.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

/**
 * TODO is this needed? maybe because of setName()
 */
class JccRegexprSpecImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccRegexprSpec {

    override val pattern: Regex? by lazy { regularExpression.toPattern(prefixMatch = false) }
    override val prefixPattern: Regex? by lazy { regularExpression.toPattern(prefixMatch = true) }

    override val production: JccRegularExprProduction
        get() = parent as JccRegularExprProduction

    override val regexKind: RegexKind
        get() = production.regexprKind.modelConstant

    override val javaBlock: JccJavaBlock?
        get() = findChildByClass(JccJavaBlock::class.java)

    override val regularExpression: JccRegularExpression
        get() = findNotNullChildByClass(JccRegularExpression::class.java)

    override val lexicalState: JccIdentifier?
        get() = findChildByClass(JccIdentifier::class.java)

    override fun getName(): String? = regularExpression.let { it as? JccNamedRegularExpression }?.name

    fun accept(visitor: JccVisitor) {
        visitor.visitRegexprSpec(this)
    }

    override fun setName(name: String): PsiElement {
        regularExpression.let { it as? JccNamedRegularExpression }?.setName(name)
        return this
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}