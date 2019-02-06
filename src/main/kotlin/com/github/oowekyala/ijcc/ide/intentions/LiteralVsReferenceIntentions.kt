package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createRegexpElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement


// These two intentions are dual: switch between token reference vs token literal


class TokenInliningIntention : JccEditorIntentionBase("Inline literal reference") {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
            element.takeIf { it.node.elementType == JccTypes.JCC_IDENT }
                ?.let { it.parent as? JccIdentifier }
                ?.let { it.parent as? JccTokenReferenceUnit }
                ?.let { it.typedReference.resolveToken()?.asSingleLiteral() } != null


    override fun run(project: Project, editor: Editor, element: PsiElement): () -> Unit {
        val rElt: JccTokenReferenceUnit = element.parent.parent as JccTokenReferenceUnit
        val newLiteral: JccLiteralRegexpUnit =
                rElt.typedReference
                    .resolveToken()!!
                    .asSingleLiteral()!!
                    .text
                    .let { createRegexpElement(project, it) }

        return {
            rElt.safeReplace(newLiteral)
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
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
        val name = rElt.typedReference
            .resolveToken()!!
            .name
            .let { JccElementFactory.createRegexReferenceUnit(project, it) }

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
                    createRegexpElement<JccTokenReferenceUnit>(
                        project,
                        "<$name>"
                    )
                )
            }

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)

        }
    }
}
