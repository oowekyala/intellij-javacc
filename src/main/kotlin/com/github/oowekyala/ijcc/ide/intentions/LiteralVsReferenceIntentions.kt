package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createRegexElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager


// These two intentions are dual: switch between token reference vs token literal


class TokenInliningIntention
    : JccSelfTargetingEditorIntentionBase<JccTokenReferenceRegexUnit>(
    JccTokenReferenceRegexUnit::class.java,
    "Inline literal reference"
) {

    override fun isApplicableTo(element: JccTokenReferenceRegexUnit): Boolean =
            element.typedReference.resolveToken()?.asSingleLiteral() != null

    override fun run(project: Project, editor: Editor, element: JccTokenReferenceRegexUnit): () -> Unit {
        val newLiteral: JccLiteralRegexUnit =
                element.typedReference
                    .resolveToken()!!
                    .asSingleLiteral()!!
                    .text
                    .let { createRegexElement(project, it) }

        return {
            element.safeReplace(newLiteral)
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
        }
    }
}


class ReplaceLiteralWithReferenceIntention :
    JccSelfTargetingEditorIntentionBase<JccLiteralRegexUnit>(
        JccLiteralRegexUnit::class.java,
        "Replace literal with reference"
    ) {

    override fun isApplicableTo(element: JccLiteralRegexUnit): Boolean =
            element.typedReference.resolveToken(exact = true)
                ?.let { it.isExplicit && it.name != null } == true


    override fun run(project: Project, editor: Editor, element: JccLiteralRegexUnit): () -> Unit {
        val newRegexUnit: JccTokenReferenceRegexUnit = element.typedReference
            .resolveToken(exact = true)!!
            .name!!
            .let { createRegexElement(project, "<$it>") }

        return {
            element.safeReplace(newRegexUnit)
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
        }
    }
}
