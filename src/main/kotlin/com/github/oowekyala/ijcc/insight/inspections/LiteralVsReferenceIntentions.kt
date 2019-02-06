package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

// These two intentions are dual: switch between token reference vs token literal

class TokenInliningIntention : JccIntentionBase("Inline literal reference") {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
            element.takeIf { it.node.elementType == JccTypes.JCC_IDENT }
                ?.let { it.parent as? JccIdentifier }
                ?.let { it.parent as? JccTokenReferenceUnit }
                ?.let { it.typedReference.resolveToken()?.asSingleLiteral() } != null

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val ref = element.parent.parent as JccTokenReferenceUnit

        val literal = ref.typedReference.resolveToken()!!.asSingleLiteral()!!

        ref.safeReplace(JccElementFactory.createLiteralRegexUnit(project, literal.text))
    }
}


class ReplaceLiteralWithReferenceIntention : JccIntentionBase("Replace literal with reference") {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
            element.takeIf { it.node.elementType == JccTypes.JCC_STRING_LITERAL }
                ?.let { it.parent as? JccLiteralRegexpUnit }
                ?.let { it.typedReference?.resolve() }
                ?.let { it.name != null && it.asSingleLiteral() != null } ?: false

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val ref = element.parent as JccLiteralRegexpUnit

        val name = ref.typedReference!!.resolve()!!.name!!

        ref.safeReplace(JccElementFactory.createRegexReferenceUnit(project, "<$name>"))
    }
}
