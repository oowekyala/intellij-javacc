package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInsight.daemon.impl.actions.AbstractBatchSuppressByNoInspectionCommentFix
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.SuppressionUtil.*
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet


class JccInspectionSuppressor : InspectionSuppressor {
    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> =
            element?.let {
                arrayOf<SuppressQuickFix>(
                    SuppressIntention(it, toolId),
                    SuppressIntention(it, ALL)
                )
            } ?: emptyArray()

    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        val containerComments = getContainerElt(element)?.leadingComments() ?: emptySequence()

        val allComments = sequenceOf(containerComments, element.leadingComments()).flatten()

        return allComments.any { comment ->
            val matcher = SUPPRESS_IN_LINE_COMMENT_PATTERN.matcher(comment)
            matcher.matches() && isInspectionToolIdMentioned(matcher.group(1), toolId)
        }
    }


    companion object {

        private val RegexpSpecIgnoreSet = TokenSet.create(TokenType.WHITE_SPACE, JavaccTypes.JCC_UNION /* | */)

        class SuppressIntention(elt: PsiElement, toolId: String)
            : AbstractBatchSuppressByNoInspectionCommentFix(toolId, toolId == ALL) {

            init {
                text = when (toolId) {
                    ALL  -> "Suppress all inspections for ${containerName(elt)}"
                    else -> "Suppress for ${containerName(elt)}"
                }
            }

            override fun getContainer(context: PsiElement?): PsiElement? = context?.let { getContainerElt(it) }
        }

        private fun containerName(element: PsiElement) =
                when (getContainerElt(element)) {
                    is JccFile                                               -> "file"
                    is JccRegexprSpec                                        -> "token specification"
                    is JccNonTerminalProduction, is JccRegularExprProduction -> "production"
                    else                                                     -> "element"
                }

        private fun getContainerElt(element: PsiElement): PsiElement? =
                element.parentSequence(includeSelf = true).firstOrNull {
                    it is JccFile || it is JccNonTerminalProduction || it is JccRegexprSpec || it is JccRegularExprProduction
                }


        private fun PsiElement.leadingComments(): Sequence<String> {

            val ignoredSet = when (this) {
                is JccRegexprSpec -> RegexpSpecIgnoreSet
                else              -> JccTypesExt.WhitespaceTypeSet
            }


            return siblingSequence(forward = false)
                .takeWhile { ignoredSet.contains(it.node.elementType) || it.isJccComment }
                .filter { it.isJccComment }
                .map { it.trimCommentMarkers.trim() }
        }
    }
}
