package com.github.oowekyala.ijcc.highlight

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.JccJjtreeNodeDescriptor
import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.JavaTokenType
import com.intellij.psi.tree.IElementType
import java.util.*

/**
 * Highlighting classes for Javacc, provides the highlighting to the [JavaccColorSettingsPage]
 * and to [JavaccSyntaxHighlighter]s.
 */
enum class JavaccHighlightingColors(base: TextAttributesKey) {
    JAVACC_KEYWORD(DefaultLanguageHighlighterColors.KEYWORD),
    JAVA_KEYWORD(JavaHighlightingColors.KEYWORD),

    /** For [JccJjtreeNodeDescriptor]. */
    JJTREE_DECORATION(JavaHighlightingColors.ANNOTATION_NAME_ATTRIBUTES),

    PARENTHESES(JavaHighlightingColors.PARENTHESES),
    DOT(JavaHighlightingColors.DOT),
    COMMA(JavaHighlightingColors.COMMA),
    SEMICOLON(JavaHighlightingColors.JAVA_SEMICOLON),
    BRACKETS(JavaHighlightingColors.BRACKETS),
    OPERATOR(JavaHighlightingColors.OPERATION_SIGN),

    STRING(JavaHighlightingColors.STRING),
    CHARACTER(JavaHighlightingColors.STRING),
    NUMBER(JavaHighlightingColors.NUMBER),

    LINE_COMMENT(JavaHighlightingColors.LINE_COMMENT),
    C_COMMENT(JavaHighlightingColors.JAVA_BLOCK_COMMENT),
    DOC_COMMENT(JavaHighlightingColors.DOC_COMMENT),

    BAD_CHARACTER(JavaHighlightingColors.INVALID_STRING_ESCAPE);

    val keys: TextAttributesKey =
        TextAttributesKey.createTextAttributesKey("JavaCC.$name", base)

    val displayName = name.removePrefix("JavaCC.")
        .replace('_', ' ')
        .toLowerCase().capitalize()
        .replace("Jjtree", "JJTree")
        .replace("Javacc", "JavaCC")


    companion object {
        private val TOKEN_TYPE_TO_STYLE: Map<IElementType, TextAttributesKey>
        fun getTokenHighlight(tokenType: IElementType?): TextAttributesKey? = TOKEN_TYPE_TO_STYLE[tokenType]

        init {
            val keys =
                HashMap<IElementType, TextAttributesKey>()

            operator fun JavaccHighlightingColors.invoke(vararg tokenType: IElementType) {
                tokenType.forEach { keys[it] = this.keys }
            }

            LINE_COMMENT(JavaccTypes.JCC_END_OF_LINE_COMMENT)
            DOC_COMMENT(JavaccTypes.JCC_DOC_COMMENT)
            C_COMMENT(JavaccTypes.JCC_C_STYLE_COMMENT)

            JAVACC_KEYWORD(
                JavaccTypes.JCC_LOOKAHEAD_KEYWORD,
                JavaccTypes.JCC_PARSER_BEGIN_KEYWORD,
                JavaccTypes.JCC_PARSER_END_KEYWORD,
                JavaccTypes.JCC_JAVACODE_KEYWORD,
                JavaccTypes.JCC_TOKEN_KEYWORD,
                JavaccTypes.JCC_SPECIAL_TOKEN_KEYWORD,
                JavaccTypes.JCC_MORE_KEYWORD,
                JavaccTypes.JCC_SKIP_KEYWORD,
                JavaccTypes.JCC_TOKEN_MGR_DECLS_KEYWORD,
                JavaccTypes.JCC_EOF_KEYWORD
            )

            JAVA_KEYWORD(
                JavaccTypes.JCC_PRIMITIVE_TYPE,
                JavaccTypes.JCC_FALSE_KEYWORD,
                JavaccTypes.JCC_TRUE_KEYWORD,
                JavaTokenType.NULL_KEYWORD,
                JavaTokenType.CLASS_KEYWORD,
                JavaTokenType.INTERFACE_KEYWORD,
                JavaTokenType.RETURN_KEYWORD,
                JavaTokenType.SUPER_KEYWORD,
                JavaTokenType.THIS_KEYWORD,
                JavaTokenType.THROW_KEYWORD,
                JavaTokenType.ABSTRACT_KEYWORD,
                JavaccTypes.JCC_PRIVATE_KEYWORD,
                JavaccTypes.JCC_PROTECTED_KEYWORD,
                JavaccTypes.JCC_PUBLIC_KEYWORD,
                JavaccTypes.JCC_THROWS_KEYWORD,
                JavaccTypes.JCC_TRY_KEYWORD,
                JavaccTypes.JCC_CATCH_KEYWORD,
                JavaccTypes.JCC_FINALLY_KEYWORD,
                JavaccTypes.JCC_VOID_KEYWORD
            )

            OPERATOR(
                JavaccTypes.JCC_GT,
                JavaccTypes.JCC_LT,
                JavaccTypes.JCC_TILDE,
                JavaccTypes.JCC_COLON,
                JavaccTypes.JCC_EQ,
                JavaccTypes.JCC_PLUS,
                JavaccTypes.JCC_MINUS
            )

            NUMBER(
                JavaccTypes.JCC_INTEGER_LITERAL,
                JavaccTypes.JCC_FLOAT_LITERAL,
                JavaccTypes.JCC_DOUBLE_LITERAL,
                JavaccTypes.JCC_LONG_LITERAL
            )



            SEMICOLON(JavaccTypes.JCC_SEMICOLON)
            COMMA(JavaccTypes.JCC_COMMA)
            DOT(JavaccTypes.JCC_DOT)
            PARENTHESES(
                JavaccTypes.JCC_LPARENTH,
                JavaccTypes.JCC_RPARENTH
            )
            BRACKETS(
                JavaccTypes.JCC_LBRACKET,
                JavaccTypes.JCC_RBRACKET
            )


            STRING(JavaccTypes.JCC_STRING_LITERAL)
            CHARACTER(JavaccTypes.JCC_CHARACTER_LITERAL)

            BAD_CHARACTER(JavaccTypes.JCC_BAD_CHARACTER)

            TOKEN_TYPE_TO_STYLE = Collections.unmodifiableMap(keys)
        }
    }

}