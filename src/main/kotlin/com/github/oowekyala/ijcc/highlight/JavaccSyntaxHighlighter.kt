package com.github.oowekyala.ijcc.highlight

import com.github.oowekyala.ijcc.lang.lexer.JavaccLexerAdapter
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

/**
 * Syntax highlighter.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        val highlight =
                JavaccHighlightingColors.getTokenHighlight(tokenType)
        return when (highlight) {
            null -> emptyArray()
            else -> arrayOf(highlight)
        }
    }


    override fun getHighlightingLexer(): Lexer = JavaccLexerAdapter()
}

