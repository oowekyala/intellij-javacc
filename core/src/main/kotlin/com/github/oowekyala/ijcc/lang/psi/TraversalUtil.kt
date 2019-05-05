package com.github.oowekyala.ijcc.lang.psi

import com.intellij.openapi.util.Conditions
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.SyntaxTraverser.psiTraverser
import com.intellij.psi.tree.IElementType
import org.intellij.grammar.parser.GeneratedParserUtilBase

/**
 * @author Clément Fournier
 * @since 1.0
 */


fun grammarTraverser(root: PsiElement): SyntaxTraverser<PsiElement> =
    psiTraverser().withRoot(root)
        .forceDisregardTypes(Conditions.equalTo<IElementType>(GeneratedParserUtilBase.DUMMY_BLOCK))
        .filter(Conditions.instanceOf<PsiElement>(JccPsiElement::class.java))

fun grammarTraverserNoJava(root: PsiElement): SyntaxTraverser<PsiElement> =
    grammarTraverser(root)
        .forceIgnore {
            when (it) {
                is JccJavaCompilationUnit, is JccJavaBlock, is JccJavaExpression -> true
                else                                                             -> false
            }
        }

fun grammarTraverserOnlyBnf(root: PsiElement): SyntaxTraverser<PsiElement> =
    grammarTraverserNoJava(root)
        .forceIgnore(Conditions.instanceOf(JccOptionSection::class.java))
        .forceIgnore(Conditions.instanceOf(JccRegexProduction::class.java))
