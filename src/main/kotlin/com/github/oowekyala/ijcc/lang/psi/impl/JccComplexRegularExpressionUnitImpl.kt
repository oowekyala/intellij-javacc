// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.JavaccTypes.JCC_STRING_LITERAL
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class JccComplexRegularExpressionUnitImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccComplexRegularExpressionUnit {

    override val characterList: JccCharacterList?
        get() = findChildByClass(JccCharacterList::class.java)

    override val complexRegularExpressionChoices: JccComplexRegularExpressionChoices?
        get() = findChildByClass(JccComplexRegularExpressionChoices::class.java)

    override val identifier: JccIdentifier?
        get() = findChildByClass(JccIdentifier::class.java)

    override val oneOrMore: JccOneOrMore?
        get() = findChildByClass(JccOneOrMore::class.java)

    override val repetitionRange: JccRepetitionRange?
        get() = findChildByClass(JccRepetitionRange::class.java)

    override val zeroOrMore: JccZeroOrMore?
        get() = findChildByClass(JccZeroOrMore::class.java)

    override val zeroOrOne: JccZeroOrOne?
        get() = findChildByClass(JccZeroOrOne::class.java)

    override val stringLiteral: PsiElement?
        get() = findChildByType(JCC_STRING_LITERAL)

    fun accept(visitor: JccVisitor) {
        visitor.visitComplexRegularExpressionUnit(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
