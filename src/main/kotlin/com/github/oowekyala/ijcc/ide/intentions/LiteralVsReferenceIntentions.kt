package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement


// These two intentions are dual: switch between token reference vs token literal


class TokenInliningIntention : JccEditorIntentionBase("Inline literal reference") {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
            element.takeIf { it.node.elementType == JccTypes.JCC_IDENT }
                ?.let { it.parent as? JccIdentifier }
                ?.let { it.parent as? JccTokenReferenceUnit }
                ?.let { it.typedReference.resolveToken()?.asSingleLiteral() } != null


    override fun run(project: Project, editor: Editor, element: PsiElement): () -> Unit {
        val rElt = element.parent.parent as JccTokenReferenceUnit
        val literal = rElt.typedReference.resolveToken()!!.asSingleLiteral()!!.text

        val container = rElt.parent

        return {
            when (container) {
                // replacing the whole regex is necessary!
                // TODO abstract that away
                is JccRegularExpressionReference -> container.safeReplace(
                    JccElementFactory.createRegex<JccLiteralRegularExpression>(
                        project,
                        literal
                    )
                )

                else                             -> rElt.safeReplace(
                    JccElementFactory.createLiteralRegexUnit(
                        project,
                        literal
                    )
                )
            }
        }
    }
}


class ReplaceLiteralWithReferenceIntention : JccEditorIntentionBase("Replace literal with reference") {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
            element.takeIf { it.node.elementType == JccTypes.JCC_STRING_LITERAL }
                ?.let { it.parent as? JccLiteralRegexpUnit }
                ?.let { it.typedReference.resolveToken(exact = true) }
                ?.let { it.isExplicit && it.name != null } ?: false

    override fun run(project: Project, editor: Editor, element: PsiElement): () -> Unit {
        val rElt = element.parent as JccLiteralRegexpUnit
        val name = rElt.typedReference.resolveToken(exact = true)!!.name!!

        val container = rElt.parent

        return {
            when (container) {
                // replacing the whole regex is necessary!
                is JccLiteralRegularExpression -> container.safeReplace(
                    JccElementFactory.createRegex<JccRegularExpressionReference>(
                        project,
                        "<$name>"
                    )
                )

                else                           -> rElt.safeReplace(
                    JccElementFactory.createRegexReferenceUnit(
                        project,
                        "<$name>"
                    )
                )
            }
        }
    }
}
