package com.github.oowekyala.ijcc.insight.highlight

import com.github.oowekyala.ijcc.insight.model.JavaccConfig.Companion.knownOptions
import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.filterMapAs
import com.github.oowekyala.ijcc.util.ifTrue
import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.strictParents
import org.apache.commons.lang3.StringEscapeUtils

/**
 * Simplest and quickest passes of highlighting.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccAnnotator : JccBaseAnnotator() {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is JccJjtreeNodeDescriptor            -> holder.highlightNodeDescriptor(element)
            is JccOptionBinding                   -> holder.validateOptionBinding(element)
            is JccJavaNonTerminalProductionHeader ->
                holder.addHighlight(element.nameIdentifier, JavaHighlightingColors.METHOD_DECLARATION_ATTRIBUTES)
            is JccOptionSection                   -> // highlight the "options" as a keyword
                holder.addHighlight(element.firstChild, JavaccHighlightingColors.JAVACC_KEYWORD.keys)
            is JccNonTerminalExpansionUnit        ->
                holder.highlightOrFlagReference(element, JavaccHighlightingColors.NONTERMINAL_REFERENCE.keys)
            is JccRegularExprProduction           ->
                element.lexicalStateList?.identifierList?.forEach {
                    holder.addHighlight(it, JavaccHighlightingColors.LEXICAL_STATE.keys)
                }
            is JccRegularExpressionReference      ->
                holder.highlightOrFlagReference(element, JavaccHighlightingColors.TOKEN_REFERENCE.keys)
            is JccLiteralRegularExpression        ->
                holder.highlightStringOrToken(element)
            is JccCharacterDescriptor             -> holder.validateCharDescriptor(element)
            is JccTryCatchExpansionUnit           -> holder.validateTryCatch(element)
            // DONE
            is JccNamedRegularExpression          -> holder.validateNameDuplicates(element)

            is JccRegexprSpec                     -> {
                // highlight the name of a global named regex
                element.regularExpression
                    .let { it as? JccNamedRegularExpression }
                    ?.run {
                        holder.addHighlight(nameIdentifier, JavaccHighlightingColors.TOKEN_REFERENCE.keys)
                    }
                element.lexicalState?.let { holder.addHighlight(it, JavaccHighlightingColors.LEXICAL_STATE.keys) }
                holder.validateRegexprSpec(element)
            }
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


    private fun AnnotationHolder.validateNameDuplicates(element: JccNamedRegularExpression) {
        element.containingFile
            .descendantSequence()
            .filterMapAs<JccNamedRegularExpression>()
            .filter { element !== it && it.name == element.name }
            .any()
            .ifTrue {
                // there was at least one duplicate
                createErrorAnnotation(element, "Multiply defined lexical token name \"${element.name}\"")
            }
    }

    private fun AnnotationHolder.validateOptionBinding(element: JccOptionBinding) {
        val opt = knownOptions[element.name]
        if (opt == null) {
            addHighlight(
                element.nameIdentifier!!, // may not be supported for some elements (eg JjtNodeDescriptor)
                CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES,
                HighlightSeverity.ERROR,
                "Unknown option: ${element.name}"
            )
            return
        }

        if (!element.matchesType(opt.expectedType)) {
            element.optionValue?.let { createErrorAnnotation(it, "Expected ${opt.expectedType}") }
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
            .filter { it !== element }
            .any { getAsLiteral(it.regularExpression)?.textMatches(regex) == true }
            .ifTrue {
                createErrorAnnotation(element, "Duplicate definition of string token ${regex.text}")
            }
    }

    private fun AnnotationHolder.highlightStringOrToken(literal: JccLiteralRegularExpression) {
        val ref: JccRegexprSpec? = literal.reference?.resolve()

        // if so, the literal declares itself
        val isSelfReferential = ref != null && literal.strictParents().any { it === ref }

        if (ref != null && !isSelfReferential) {
            addHighlight(literal, JavaccHighlightingColors.TOKEN_REFERENCE.keys, message = "Matched by a token")
        } // else stay default
    }

    private fun AnnotationHolder.highlightNodeDescriptor(nodeDescriptor: JccJjtreeNodeDescriptor) {
        // extracts the range of the "#" + the range of the ident or "void" kword
        fun getRangeFor(element: JccJjtreeNodeDescriptor): TextRange {
            val ident = element.nameIdentifier
            val poundRange = element.firstChild.textRange // "#"
            return if (ident != null) poundRange.union(ident.textRange)
            else element.node.getChildren(TokenSet.create(JavaccTypes.JCC_VOID_KEYWORD))
                .firstOrNull()
                ?.let { poundRange.union(it.textRange) }
                ?: poundRange
        }

        val header = nodeDescriptor.productionHeader
        val expansionUnit = nodeDescriptor.expansionUnit

        if (header == null && expansionUnit != null) {
            addHighlight(
                trimWhitespace(expansionUnit),
                JavaccHighlightingColors.JJTREE_NODE_SCOPE.keys,
                message = "In the node scope of #${nodeDescriptor.name ?: "void"}"
            )
            addHighlight(getRangeFor(nodeDescriptor), JavaccHighlightingColors.JJTREE_DECORATION.keys)
            if (nodeDescriptor.isVoid) {
                createWeakWarningAnnotation(getRangeFor(nodeDescriptor), "Useless #void annotation")
            }
        } else if (header != null && expansionUnit == null) {
            val message =
                    if (nodeDescriptor.isVoid) "Discards the node created by this production"
                    else "Renames this production's node to ${nodeDescriptor.name}"

            addHighlight(
                getRangeFor(nodeDescriptor),
                JavaccHighlightingColors.JJTREE_DECORATION.keys,
                message = message
            )
        } else {
            createErrorAnnotation(getRangeFor(nodeDescriptor), "Dangling node descriptor")
        }
    }

}