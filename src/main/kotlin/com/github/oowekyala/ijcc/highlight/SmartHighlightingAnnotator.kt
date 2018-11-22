package com.github.oowekyala.ijcc.highlight

import com.github.oowekyala.ijcc.highlight.JavaccHighlightingColors.*
import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet


/**
 * Complements the syntax highlighting lexer with some syntactic information.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class SmartHighlightingAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is JccJjtreeNodeDescriptor            -> holder.highlightNodeDescriptor(element)
            is JccJavaNonTerminalProductionHeader ->
                holder.addHighlight(element.nameIdentifier, JavaHighlightingColors.METHOD_DECLARATION_ATTRIBUTES)
            is JccJavaccOptions                   -> // highlight the "options" as a keyword
                holder.addHighlight(element.firstChild, JAVACC_KEYWORD.keys)
            is JccRegexprSpec                     ->
                // highlight the name of a global named regex
                element.regularExpression
                    .let { it as? JccNamedRegularExpression }
                    ?.run {
                        holder.addHighlight(nameIdentifier, TOKEN.keys)
                    }
            is JccRegularExpressionReference      ->
                holder.highlightOrFlagReference(element, TOKEN.keys)
            is JccNonTerminalExpansionUnit        -> {
                holder.highlightOrFlagReference(element, NONTERMINAL_REFERENCE.keys)
            }
            is JccLiteralRegularExpression        -> {
                // highlight string literals covered by a regexp spec
                element.reference?.resolve()?.run {
                    // if not resolved, the highlight stays the default for string
                    holder.addHighlight(element, TOKEN.keys, message = "Matched by a token")
                }
            }
        }
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

    private fun trimWhitespace(psiElement: PsiElement): TextRange {
        var range = psiElement.textRange
        if (psiElement.firstChild.node.elementType == TokenType.WHITE_SPACE) {
            range = range.cutOut(psiElement.firstChild.textRange)
        }

        if (psiElement.lastChild.node.elementType == TokenType.WHITE_SPACE) {
            range = range.cutOut(psiElement.lastChild.textRange)
        }

        return range
    }


    private fun AnnotationHolder.addHighlight(element: PsiElement,
                                              textAttributesKey: TextAttributesKey,
                                              severity: HighlightSeverity = HighlightSeverity.INFORMATION,
                                              message: String? = null) {
        addHighlight(element.textRange, textAttributesKey, severity, message)
    }

    private fun AnnotationHolder.addHighlight(textRange: TextRange,
                                              textAttributesKey: TextAttributesKey,
                                              severity: HighlightSeverity = HighlightSeverity.INFORMATION,
                                              message: String? = null) {
        val annotation = this.createAnnotation(severity, textRange, message)
        annotation.textAttributes = textAttributesKey
    }

}