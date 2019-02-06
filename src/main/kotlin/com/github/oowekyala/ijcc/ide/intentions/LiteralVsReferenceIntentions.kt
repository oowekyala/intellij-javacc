package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createRegexpElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager


// These two intentions are dual: switch between token reference vs token literal


class TokenInliningIntention
    : JccSelfTargetingEditorIntentionBase<JccTokenReferenceUnit>(
    JccTokenReferenceUnit::class.java,
    "Inline literal reference"
) {

    override fun isApplicableTo(element: JccTokenReferenceUnit): Boolean =
            element.typedReference.resolveToken()?.asSingleLiteral() != null

    override fun run(project: Project, editor: Editor, element: JccTokenReferenceUnit): () -> Unit {
        val newLiteral: JccLiteralRegexpUnit =
                element.typedReference
                    .resolveToken()!!
                    .asSingleLiteral()!!
                    .text
                    .let { createRegexpElement(project, it) }

        return {
            element.safeReplace(newLiteral)
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
        }
    }
}


class ReplaceLiteralWithReferenceIntention :
    JccSelfTargetingEditorIntentionBase<JccLiteralRegexpUnit>(
        JccLiteralRegexpUnit::class.java,
        "Replace literal with reference"
    ) {

    override fun isApplicableTo(element: JccLiteralRegexpUnit): Boolean =
            element.typedReference.resolveToken(exact = true)
                ?.let { it.isExplicit && it.name != null } == true


    override fun run(project: Project, editor: Editor, element: JccLiteralRegexpUnit): () -> Unit {
        val newUnit: JccTokenReferenceUnit = element.typedReference
            .resolveToken(exact = true)!!
            .name!!
            .let { createRegexpElement(project, "<$it>") }

        return {
            element.safeReplace(newUnit)
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
        }
    }
}
