package com.github.oowekyala.ijcc.highlight

import com.github.oowekyala.ijcc.highlight.JavaccHighlightingColors.*
import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.strictParents


/**
 * Complements the syntax highlighting lexer with some syntactic information.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class SmartHighlightingAnnotator : JccBaseAnnotator() {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is JccJjtreeNodeDescriptor            -> holder.highlightNodeDescriptor(element)
            is JccJavaNonTerminalProductionHeader ->
                holder.addHighlight(element.nameIdentifier, JavaHighlightingColors.METHOD_DECLARATION_ATTRIBUTES)
            is JccOptionSection                   -> // highlight the "options" as a keyword
                holder.addHighlight(element.firstChild, JAVACC_KEYWORD.keys)
            is JccRegexprSpec                     ->
                // highlight the name of a global named regex
                element.regularExpression
                    .let { it as? JccNamedRegularExpression }
                    ?.run {
                        holder.addHighlight(nameIdentifier, TOKEN.keys)
                    }
            is JccRegularExpression -> holder.dealWithRegexp(element)
            is JccNonTerminalExpansionUnit        ->
                holder.highlightOrFlagReference(element, NONTERMINAL_REFERENCE.keys)
        }
    }

    private fun AnnotationHolder.dealWithRegexp(element: JccRegularExpression) {

        when (element) {
            is JccRegularExpressionReference ->
                highlightOrFlagReference(element, TOKEN.keys)
            is JccLiteralRegularExpression   ->
                highlightStringOrToken(element)
        }
    }

    private fun AnnotationHolder.highlightStringOrToken(literal: JccLiteralRegularExpression) {
        val ref = literal.reference?.resolve()

        // if so, the literal declares itself
        val isSelfReferential = ref != null && literal.strictParents().any { it === ref }

        if (ref != null && !isSelfReferential) {
            addHighlight(literal, TOKEN.keys, message = "Matched by a token")
        } // else stay default
    }

    private fun AnnotationHolder.highlightOrFlagReference(element: JccIdentifierOwner, normalKeys: TextAttributesKey) {
        if (element.reference?.resolve() == null) {
            addHighlight(
                element.nameIdentifier!!, // may not be supported for some elements (eg JjtNodeDescriptor)
                CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES,
                HighlightSeverity.ERROR,
                "Unresolved reference: ${element.name}"
            )
        } else {
            addHighlight(element.nameIdentifier!!, normalKeys)
        }
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
                JJTREE_NODE_SCOPE.keys,
                message = "In the node scope of #${nodeDescriptor.name ?: "void"}"
            )
            addHighlight(getRangeFor(nodeDescriptor), JJTREE_DECORATION.keys)
            if (nodeDescriptor.isVoid) {
                createWeakWarningAnnotation(getRangeFor(nodeDescriptor), "Useless #void annotation")
            }
        } else if (header != null && expansionUnit == null) {
            val message =
                    if (nodeDescriptor.isVoid) "Discards the node created by this production"
                    else "Renames this production's node to ${nodeDescriptor.name}"

            addHighlight(getRangeFor(nodeDescriptor), JJTREE_DECORATION.keys, message = message)
        } else {
            createErrorAnnotation(getRangeFor(nodeDescriptor), "Dangling node descriptor")
        }
    }


}