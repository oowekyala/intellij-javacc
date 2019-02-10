package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.util.contains
import com.github.oowekyala.ijcc.util.prepend
import com.intellij.codeInsight.daemon.impl.actions.AbstractBatchSuppressByNoInspectionCommentFix
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.SuppressionUtil
import com.intellij.codeInspection.SuppressionUtil.ALL
import com.intellij.codeInspection.SuppressionUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.ThreeState
import com.siyeh.ig.javadoc.DanglingJavadocInspection
import java.util.regex.Pattern

/**
 * Allows suppressing inspections.
 */
object JccInspectionSuppressor : InspectionSuppressor {
    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        val seq: Sequence<SuppressQuickFix> = element?.let {
            val containers: Sequence<SuppressQuickFix> = it.getAllContainers().map {
                SuppressIntention(
                    it,
                    toolId
                )
            }
            val nearest = it.getNearestContainer()

            if (nearest != null) containers.prepend(SuppressIntention(nearest, ALL))
            else containers
        } ?: emptySequence()

        return seq.toList().toTypedArray()
    }

    private val AlwaysSuppressedInspections = setOf(
        /*
        TODO
          the plain Java DanglingJavadoc inspection doesn't consider
          Nonterminal productions as worthy. Introduce a JavaCC-specific
          similar inspection
        */
        DanglingJavadocInspection().id
    )

    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean =
        element is JccPsiElement && (
            toolId in AlwaysSuppressedInspections
                || element.ancestors(includeSelf = true).any { isSuppressedByComment(it, toolId) }
            )

    private fun isSuppressedByComment(element: PsiElement, toolId: String): Boolean {
        return element.leadingComments().any { comment ->
            val matcher = SuppressRegex.matcher(comment)
            matcher.matches() && SuppressionUtil.isInspectionToolIdMentioned(matcher.group(1), toolId)
        }
    }


    private val SuppressRegex = Pattern.compile(SuppressionUtil.COMMON_SUPPRESS_REGEXP)

    private val RegexSpecIgnoreSet = TokenSet.create(TokenType.WHITE_SPACE, JccTypes.JCC_UNION /* | */)

    /**
     * Intention to suppress an inspection.
     */
    class SuppressIntention(elt: PsiElement, toolId: String)
        : AbstractBatchSuppressByNoInspectionCommentFix(toolId, toolId == ALL) {

        init {
            text = when (toolId) {
                ALL  -> "Suppress all inspections for ${containerName(elt)}"
                else -> "Suppress for ${containerName(elt)}"
            }
            isShouldBeAppliedToInjectionHost = ThreeState.NO
        }

        override fun getContainer(context: PsiElement?): PsiElement? = context?.getNearestContainer()

        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            val text = SuppressionUtilCore.SUPPRESS_INSPECTIONS_TAG_NAME + " " + myID
            JccElementFactory.insertEolCommentBefore(project, container, text)
        }
    }

    private fun containerName(element: PsiElement) =
        when (element.getNearestContainer()) {
            is JccFile                                         -> "file"
            is JccRegexSpec                                    -> "token specification"
            is JccNonTerminalProduction, is JccRegexProduction -> "production"
            else                                               -> "element"
        }

    private fun PsiElement.isContainer(): Boolean =
        this is JccFile || this is JccNonTerminalProduction || this is JccRegexSpec || this is JccRegexProduction

    private fun PsiElement.getNearestContainer(): PsiElement? = getAllContainers().firstOrNull()

    private fun PsiElement.getAllContainers(): Sequence<PsiElement> =
        ancestors(includeSelf = true).filter { it.isContainer() }

    private fun PsiElement.leadingComments(): Sequence<String> {

        val ignoredSet = when (this) {
            is JccRegexSpec -> RegexSpecIgnoreSet
            else            -> JccTypesExt.WhitespaceTypeSet
        }


        return siblingSequence(forward = false)
            .filterNotNull()
            .takeWhile { ignoredSet.contains(it) || it.isJccComment }
            .filter { it.isJccComment }
            .map { it.trimCommentMarkers.trim() }
    }
}
