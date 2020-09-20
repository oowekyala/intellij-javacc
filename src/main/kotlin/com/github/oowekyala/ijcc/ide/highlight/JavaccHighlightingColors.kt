package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.lang.JccTypes.*
import com.github.oowekyala.ijcc.lang.psi.JccJjtreeNodeDescriptor
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
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
    /**
     * For [JccJjtreeNodeDescriptor.expansionUnit]. Keys are provided by some of
     * the resource files.
     */
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
                JCC_PRIVATE_KEYWORD,
                JCC_PROTECTED_KEYWORD,
                JCC_PUBLIC_KEYWORD,
                JCC_THROWS_KEYWORD,
                JCC_TRY_KEYWORD,
                JCC_CATCH_KEYWORD,
                JCC_FINALLY_KEYWORD,
                JCC_VOID_KEYWORD,


                JCC_NULL_KEYWORD,

                JCC_ASSERT_KEYWORD,   // since 1.4
                JCC_ENUM_KEYWORD,     // since 1.5

                JCC_ABSTRACT_KEYWORD,
                JCC_BREAK_KEYWORD,
                JCC_CASE_KEYWORD,
                JCC_CLASS_KEYWORD,
                JCC_CONST_KEYWORD,
                JCC_CONTINUE_KEYWORD,
                JCC_DEFAULT_KEYWORD,
                JCC_DO_KEYWORD,
                JCC_ELSE_KEYWORD,

                JCC_EXTENDS_KEYWORD,
                JCC_FINAL_KEYWORD,
                JCC_FOR_KEYWORD,
                JCC_GOTO_KEYWORD,
                JCC_IF_KEYWORD,
                JCC_IMPLEMENTS_KEYWORD,
                JCC_IMPORT_KEYWORD,
                JCC_INSTANCEOF_KEYWORD,
                JCC_INTERFACE_KEYWORD,
                JCC_NATIVE_KEYWORD,
                JCC_NEW_KEYWORD,
                JCC_PACKAGE_KEYWORD,
                JCC_SUPER_KEYWORD,
                JCC_SWITCH_KEYWORD,
                JCC_SYNCHRONIZED_KEYWORD,
                JCC_THIS_KEYWORD,
                JCC_THROW_KEYWORD,
                JCC_TRANSIENT_KEYWORD,
                JCC_RETURN_KEYWORD,
                JCC_STATIC_KEYWORD,
                JCC_STRICTFP_KEYWORD,
                JCC_WHILE_KEYWORD,
                JCC_VOLATILE_KEYWORD
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

                JCC_EQEQ,
                JCC_NE,
                JCC_OROR,
                JCC_PLUSPLUS,
                JCC_MINUSMINUS,
                JCC_ANDAND,

                JCC_AND,
                JCC_LE,
                JCC_GE,

                JCC_PLUSEQ,
                JCC_MINUSEQ,
                JCC_ASTERISKEQ,
                JCC_DIVEQ,
                JCC_ANDEQ,
                JCC_OREQ,
                JCC_XOREQ,
                JCC_PERCEQ,

                JCC_EXCL,
                JCC_DIV,
                JCC_PERC,
                JCC_AT,

                JCC_DOUBLE_COLON,
                JCC_ARROW
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
