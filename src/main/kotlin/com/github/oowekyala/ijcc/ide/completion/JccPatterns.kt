package com.github.oowekyala.ijcc.ide.completion

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.JccBnfProduction
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.instanceOf
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType

/**
 * Patterns for auto completion.
 *
 * TODO we can use those to simplify live template contexts
 *
 * @author Clément Fournier
 * @since 1.2
 */
object JccPatterns {

    val placePattern: ElementPattern<PsiElement> = psiElement()
        .inFile(instanceOf(JccFile::class.java))
        .andNot(psiElement().inside(PsiComment::class.java))

    val optionValuePattern: ElementPattern<PsiElement> =
        psiElement()
            .withAncestor(2, psiElement(JccOptionBinding::class.java))
            .afterLeafSkipping(psiElement(TokenType.ERROR_ELEMENT), psiElement(JccTypes.JCC_EQ))

    val optionNamePattern: ElementPattern<PsiElement> =
        psiElement()
            .atStartOf(psiElement(JccOptionBinding::class.java))
            .andNot(psiElement().inside(PsiComment::class.java))
            .andNot(optionValuePattern)

    val bnfColonPattern =
        psiElement(JccTypes.JCC_COLON).withParent(JccBnfProduction::class.java)

    val jjtreeHashPattern =
        psiElement().afterLeaf(psiElement(JccTypes.JCC_POUND))

}
