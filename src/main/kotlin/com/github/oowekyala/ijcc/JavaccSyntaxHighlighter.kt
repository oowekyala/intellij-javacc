package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.JavaccTypes.*
import com.github.oowekyala.ijcc.lang.lexer.JavaccLexerAdapter
import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaTokenType
import com.intellij.psi.tree.IElementType
import java.util.*
import kotlin.reflect.KProperty

/**
 * Syntax highlighter.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        val highlight = JavaccHighlightingColors.getTokenHighlight(tokenType)
        return when (highlight) {
            null -> emptyArray()
            else -> arrayOf(highlight)
        }
    }

    override fun getHighlightingLexer(): Lexer = JavaccLexerAdapter()
}

/** Extension point. */
class JavaccSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter =
        JavaccSyntaxHighlighter()
}

/**
 * Highlighting classes for Javacc.
 */
enum class JavaccHighlightingColors(base: KProperty<TextAttributesKey>, name: String = base.name) {
    JAVACC_KEYWORD(DefaultLanguageHighlighterColors::KEYWORD, "JAVACC_KEYWORD"),
    JAVA_KEYWORD(JavaHighlightingColors::KEYWORD, "JAVA_KEYWORD"),
    PARENTHESES(DefaultLanguageHighlighterColors::PARENTHESES),
    DOT(DefaultLanguageHighlighterColors::DOT),
    COMMA(DefaultLanguageHighlighterColors::COMMA),
    SEMICOLON(DefaultLanguageHighlighterColors::SEMICOLON),
    BRACKETS(DefaultLanguageHighlighterColors::BRACKETS),
    NUMBER(DefaultLanguageHighlighterColors::NUMBER),
    OPERATOR(DefaultLanguageHighlighterColors::OPERATION_SIGN),
    STRING(DefaultLanguageHighlighterColors::STRING),
    CHARACTER(JavaHighlightingColors::STRING, "CHARACTER"),
    LINE_COMMENT(DefaultLanguageHighlighterColors::LINE_COMMENT),
    C_COMMENT(DefaultLanguageHighlighterColors::BLOCK_COMMENT),
    DOC_COMMENT(DefaultLanguageHighlighterColors::DOC_COMMENT),
    TOKEN(DefaultLanguageHighlighterColors::KEYWORD),
    ERROR(DefaultLanguageHighlighterColors::INVALID_STRING_ESCAPE);


    val keys: TextAttributesKey =
        TextAttributesKey.createTextAttributesKey("JavaCC.$name", base.getter.call().defaultAttributes)

    val displayName = name.removePrefix("JavaCC.").replace('_', ' ').toLowerCase().capitalize()


    companion object {
        private val TOKEN_TYPE_TO_STYLE: Map<IElementType, TextAttributesKey>

        fun getTokenHighlight(tokenType: IElementType?): TextAttributesKey? = TOKEN_TYPE_TO_STYLE[tokenType]

        init {
            val keys = HashMap<IElementType, TextAttributesKey>()

            operator fun JavaccHighlightingColors.invoke(vararg tokenType: IElementType) {
                tokenType.forEach { keys[it] = this.keys }
            }

            LINE_COMMENT(JCC_END_OF_LINE_COMMENT)
            DOC_COMMENT(JCC_DOC_COMMENT)
            C_COMMENT(JCC_C_STYLE_COMMENT)

            JAVACC_KEYWORD(
                JCC_LOOKAHEAD_KEYWORD,
                JCC_PARSER_BEGIN_KEYWORD,
                JCC_PARSER_END_KEYWORD,
                JCC_JAVACODE_KEYWORD,
                JCC_TOKEN_KEYWORD,
                JCC_SPECIAL_TOKEN_KEYWORD,
                JCC_MORE_KEYWORD,
                JCC_SKIP_KEYWORD,
                JCC_TOKEN_MGR_DECLS_KEYWORD,
                JCC_EOF_KEYWORD
            )

            JAVA_KEYWORD(
                JCC_PRIMITIVE_TYPE,
                JCC_FALSE_KEYWORD,
                JCC_TRUE_KEYWORD,
                JavaTokenType.NULL_KEYWORD,
                JavaTokenType.CLASS_KEYWORD,
                JavaTokenType.INTERFACE_KEYWORD,
                JavaTokenType.RETURN_KEYWORD,
                JCC_PRIVATE_KEYWORD,
                JCC_PROTECTED_KEYWORD,
                JCC_PUBLIC_KEYWORD,
                JCC_THROWS_KEYWORD,
                JCC_TRY_KEYWORD,
                JCC_CATCH_KEYWORD,
                JCC_FINALLY_KEYWORD,
                JCC_VOID_KEYWORD
            )

            OPERATOR(
                JCC_GT,
                JCC_LT,
                JCC_TILDE,
                JCC_COLON,
                JCC_EQ,
                JCC_PLUS,
                JCC_MINUS
            )

            NUMBER(
                JCC_INTEGER_LITERAL,
                JCC_FLOAT_LITERAL,
                JCC_DOUBLE_LITERAL,
                JCC_LONG_LITERAL
            )



            SEMICOLON(JCC_SEMICOLON)
            COMMA(JCC_COMMA)
            DOT(JCC_DOT)
            PARENTHESES(JCC_LPARENTH, JCC_RPARENTH)
            BRACKETS(JCC_LBRACKET, JCC_RBRACKET)


            STRING(JCC_STRING_LITERAL)
            STRING(JCC_CHARACTER_LITERAL)

            ERROR(JCC_BAD_CHARACTER)

            TOKEN_TYPE_TO_STYLE = Collections.unmodifiableMap(keys)
        }
    }

}