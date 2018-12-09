package com.github.oowekyala.ijcc.insight.highlight

import com.github.oowekyala.ijcc.lang.JavaccTypes.*
import com.github.oowekyala.ijcc.lang.psi.JccJjtreeNodeDescriptor
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.JavaTokenType
import com.intellij.psi.tree.IElementType
import java.util.*

/**
 * Highlighting classes for Javacc, provides the highlighting to the [JavaccColorSettingsPage]
 * and to [JavaccSyntaxHighlighter]s.
 */
enum class JavaccHighlightingColors(base: TextAttributesKey?) {
    JAVACC_KEYWORD(DefaultLanguageHighlighterColors.KEYWORD),
    JAVA_KEYWORD(JavaHighlightingColors.KEYWORD),

    /** For [JccJjtreeNodeDescriptor]. */
    JJTREE_DECORATION(DefaultLanguageHighlighterColors.INTERFACE_NAME),
    /** For [JccJjtreeNodeDescriptor.expansionUnit]. */
    JJTREE_NODE_SCOPE(null) {
        override val highlightType: HighlightInfoType = createSymbolTypeInfo(HighlightSeverity.INFORMATION)
    },

    NONTERMINAL_DECLARATION(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION),
    NONTERMINAL_REFERENCE(DefaultLanguageHighlighterColors.FUNCTION_CALL),

    LEXICAL_STATE(DefaultLanguageHighlighterColors.METADATA),

    /** Name of a [JCC_OPTION_BINDING]. */
    OPTION_NAME(NONTERMINAL_DECLARATION.keys),

    PARENTHESES(JavaHighlightingColors.PARENTHESES),
    DOT(JavaHighlightingColors.DOT),
    COMMA(JavaHighlightingColors.COMMA),
    SEMICOLON(JavaHighlightingColors.JAVA_SEMICOLON),
    BRACKETS(JavaHighlightingColors.BRACKETS),
    OPERATOR_SIGN(JavaHighlightingColors.OPERATION_SIGN),
    BRACES(JavaHighlightingColors.BRACES),

    STRING(JavaHighlightingColors.STRING),

    TOKEN_DECLARATION(DefaultLanguageHighlighterColors.INSTANCE_FIELD),
    TOKEN_REFERENCE(TOKEN_DECLARATION.keys),
    TOKEN_LITERAL_REFERENCE(TOKEN_REFERENCE.keys),    // used for strings that match a token

    PRIVATE_REGEX_DECLARATION(TOKEN_DECLARATION.keys),

    CHARACTER(JavaHighlightingColors.STRING),
    NUMBER(JavaHighlightingColors.NUMBER),

    LINE_COMMENT(JavaHighlightingColors.LINE_COMMENT),
    C_COMMENT(JavaHighlightingColors.JAVA_BLOCK_COMMENT),

    BAD_CHARACTER(JavaHighlightingColors.INVALID_STRING_ESCAPE);


    val keys: TextAttributesKey = TextAttributesKey.createTextAttributesKey("JAVACC_$name", base)

    val displayName =
            name.replace('_', ' ')
                .toLowerCase().capitalize()
                .replace("Jjtree", "JJTree")
                .replace("Javacc", "JavaCC")

    open val highlightType: HighlightInfoType = createSymbolTypeInfo(HighlightInfoType.SYMBOL_TYPE_SEVERITY)

    protected fun createSymbolTypeInfo(severity: HighlightSeverity): HighlightInfoType {
        return HighlightInfoType.HighlightInfoTypeImpl(severity, keys, false)
    }

    companion object {
        private val TOKEN_TYPE_TO_STYLE: Map<IElementType, TextAttributesKey>
        fun getTokenHighlight(tokenType: IElementType?): TextAttributesKey? = TOKEN_TYPE_TO_STYLE[tokenType]

        init {
            val keys = HashMap<IElementType, TextAttributesKey>()

            operator fun JavaccHighlightingColors.invoke(vararg tokenType: IElementType) {
                tokenType.forEach { keys[it] = this.keys }
            }

            // Each token type for javacc tokens and for java tokens must fall into one of those categories

            LINE_COMMENT(JCC_END_OF_LINE_COMMENT)
            C_COMMENT(JCC_C_STYLE_COMMENT, JCC_DOC_COMMENT)

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
                JCC_PRIVATE_KEYWORD,
                JCC_PROTECTED_KEYWORD,
                JCC_PUBLIC_KEYWORD,
                JCC_THROWS_KEYWORD,
                JCC_TRY_KEYWORD,
                JCC_CATCH_KEYWORD,
                JCC_FINALLY_KEYWORD,
                JCC_VOID_KEYWORD,


                JavaTokenType.NULL_KEYWORD,

                JavaTokenType.ASSERT_KEYWORD,   // since 1.4
                JavaTokenType.ENUM_KEYWORD,     // since 1.5

                JavaTokenType.ABSTRACT_KEYWORD,
                JavaTokenType.BREAK_KEYWORD,
                JavaTokenType.CASE_KEYWORD,
                JavaTokenType.CLASS_KEYWORD,
                JavaTokenType.CONST_KEYWORD,
                JavaTokenType.CONTINUE_KEYWORD,
                JavaTokenType.DEFAULT_KEYWORD,
                JavaTokenType.DO_KEYWORD,
                JavaTokenType.ELSE_KEYWORD,

                JavaTokenType.EXTENDS_KEYWORD,
                JavaTokenType.FINAL_KEYWORD,
                JavaTokenType.FOR_KEYWORD,
                JavaTokenType.GOTO_KEYWORD,
                JavaTokenType.IF_KEYWORD,
                JavaTokenType.IMPLEMENTS_KEYWORD,
                JavaTokenType.IMPORT_KEYWORD,
                JavaTokenType.INSTANCEOF_KEYWORD,
                JavaTokenType.INTERFACE_KEYWORD,
                JavaTokenType.NATIVE_KEYWORD,
                JavaTokenType.NEW_KEYWORD,
                JavaTokenType.PACKAGE_KEYWORD,
                JavaTokenType.SUPER_KEYWORD,
                JavaTokenType.SWITCH_KEYWORD,
                JavaTokenType.SYNCHRONIZED_KEYWORD,
                JavaTokenType.THIS_KEYWORD,
                JavaTokenType.THROW_KEYWORD,
                JavaTokenType.TRANSIENT_KEYWORD,
                JavaTokenType.RETURN_KEYWORD,
                JavaTokenType.STATIC_KEYWORD,
                JavaTokenType.STRICTFP_KEYWORD,
                JavaTokenType.WHILE_KEYWORD,
                JavaTokenType.VOLATILE_KEYWORD
            )

            OPERATOR_SIGN(
                JCC_GT,
                JCC_LT,
                JCC_TILDE,
                JCC_UNION,
                JCC_COLON,
                JCC_EQ,

                JCC_PLUS,
                JCC_MINUS,
                JCC_ASTERISK,
                JCC_QUESTION,

                JavaTokenType.EQEQ,
                JavaTokenType.NE,
                JavaTokenType.OROR,
                JavaTokenType.PLUSPLUS,
                JavaTokenType.MINUSMINUS,
                JavaTokenType.ANDAND,

                JavaTokenType.AND,
                JavaTokenType.LE,
                JavaTokenType.GE,

                JavaTokenType.PLUSEQ,
                JavaTokenType.MINUSEQ,
                JavaTokenType.ASTERISKEQ,
                JavaTokenType.DIVEQ,
                JavaTokenType.ANDEQ,
                JavaTokenType.OREQ,
                JavaTokenType.XOREQ,
                JavaTokenType.PERCEQ,

                JavaTokenType.EXCL,
                JavaTokenType.DIV,
                JavaTokenType.PERC,
                JavaTokenType.AT,

                JavaTokenType.DOUBLE_COLON,
                JavaTokenType.ARROW
            )

            NUMBER(
                JCC_INTEGER_LITERAL,
                JCC_FLOAT_LITERAL,
                JCC_DOUBLE_LITERAL,
                JCC_LONG_LITERAL
            )



            SEMICOLON(JCC_SEMICOLON)
            COMMA(JCC_COMMA)
            DOT(JCC_DOT, JCC_ELLIPSIS)
            PARENTHESES(JCC_LPARENTH, JCC_RPARENTH)
            BRACKETS(JCC_LBRACKET, JCC_RBRACKET)
            BRACES(JCC_LBRACE, JCC_RBRACE)

            STRING(JCC_STRING_LITERAL)
            CHARACTER(JCC_CHARACTER_LITERAL)

            BAD_CHARACTER(JCC_BAD_CHARACTER)

            TOKEN_TYPE_TO_STYLE = Collections.unmodifiableMap(keys)
        }
    }

}