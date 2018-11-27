package com.github.oowekyala.ijcc.highlight

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.ifTrue
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import org.apache.commons.lang3.StringEscapeUtils

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccErrorAnnotator : JccBaseAnnotator() {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is JccCharacterDescriptor   -> holder.validateCharDescriptor(element)
            is JccTryCatchExpansionUnit -> holder.validateTryCatch(element)
            is JccRegexprSpec           -> holder.validateRegexprSpec(element)
        }
    }

    private fun AnnotationHolder.validateTryCatch(tryCatch: JccTryCatchExpansionUnit) {
        if (tryCatch.catchClauseList.isEmpty() && tryCatch.finallyClause == null) {
            createErrorAnnotation(tryCatch, "Try block must contain at least one catch or finally block.")
        }
    }

    private fun AnnotationHolder.validateCharDescriptor(descriptor: JccCharacterDescriptor) {

        fun AnnotationHolder.checkCharLength(psiElement: PsiElement, unescaped: String): Boolean {
            if (unescaped.length != 1) {
                createErrorAnnotation(psiElement, "String in character list may contain only one character.")
                return false
            }
            return true
        }


        val left: String = try {
            StringEscapeUtils.unescapeJava(descriptor.baseCharAsString)
        } catch (e: IllegalArgumentException) {
            createErrorAnnotation(descriptor.baseCharElement, e.message)
            return
        }
        val right: String? = try {
            StringEscapeUtils.unescapeJava(descriptor.toCharAsString)
        } catch (e: IllegalArgumentException) {
            createErrorAnnotation(descriptor.toCharElement!!, e.message)
            return
        }

        val checkRange =
                checkCharLength(descriptor.baseCharElement, left)
                        && right != null && checkCharLength(descriptor.toCharElement!!, right)

        if (checkRange && (left[0].toInt() > right!![0].toInt())) {

            createErrorAnnotation(
                descriptor,
                "Right end of character range \'$right\' has a lower ordinal value than the left end of character range \'$left\'."
            )
        }
    }

    private fun AnnotationHolder.validateRegexprSpec(element: JccRegexprSpec) {

        fun getAsLiteral(regex: JccRegexpLike): JccLiteralRegularExpression? = when (regex) {
            is JccLiteralRegularExpression -> regex
            is JccInlineRegularExpression  -> regex.regexpElement?.let { getAsLiteral(it) }
            is JccNamedRegularExpression   -> regex.regexpElement?.let { getAsLiteral(it) }
            else                           -> null
        }

        val regex = getAsLiteral(element.regularExpression) ?: return

        element.containingFile.globalTokenSpecs
            .filter { !it.isPrivate }
            .filter { it !== element }
            .any { getAsLiteral(it.regularExpression)?.textMatches(regex) == true }
            .ifTrue {
                createErrorAnnotation(element, "Duplicate definition of string token ${regex.text}")
            }
    }
}