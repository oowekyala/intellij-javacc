package com.github.oowekyala.gark87.idea.javacc

import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants
import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCLexer
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.SyntaxHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.util.Pair
import com.intellij.psi.tree.IElementType
import java.util.*
import kotlin.reflect.KProperty

/**
 * @author gark87
 */
class JavaCCHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = JavaCCLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey?> = arrayOf(TOKEN_TYPE_TO_STYLE[tokenType])

    companion object {

        private fun attributesKey(base: KProperty<TextAttributesKey>, name: String = base.name): TextAttributesKey = TextAttributesKey.createTextAttributesKey("JavaCC.$name", base.getter.call().defaultAttributes)

        val JAVACC_KEYWORD = attributesKey(SyntaxHighlighterColors::KEYWORD, "JAVACC_KEYWORD")
        val JAVA_KEYWORD = attributesKey(SyntaxHighlighterColors::KEYWORD, "JAVA_KEYWORD")
        val PARENTHS = attributesKey(SyntaxHighlighterColors::PARENTHS)
        val DOT = attributesKey(SyntaxHighlighterColors::DOT)
        val COMMA = attributesKey(SyntaxHighlighterColors::COMMA)
        val BRACKETS = attributesKey(SyntaxHighlighterColors::BRACKETS)
        val NUMBER = attributesKey(SyntaxHighlighterColors::NUMBER)
        val OPERATOR = attributesKey(SyntaxHighlighterColors::OPERATION_SIGN)
        val STRING = attributesKey(SyntaxHighlighterColors::STRING)
        val COMMENT = attributesKey(SyntaxHighlighterColors::LINE_COMMENT)
        val TOKEN = attributesKey(SyntaxHighlighterColors::KEYWORD)
        val ERROR = attributesKey(SyntaxHighlighterColors::INVALID_STRING_ESCAPE)

        val DISPLAY_NAMES: Map<TextAttributesKey, Pair<String, HighlightSeverity>>
        private val TOKEN_TYPE_TO_STYLE: Map<IElementType, TextAttributesKey>

        init {
            val keys = HashMap<IElementType, TextAttributesKey>()

            // comment
            keys[JavaCCConstants.SINGLE_LINE_COMMENT] = COMMENT
            keys[JavaCCConstants.FORMAL_COMMENT] = COMMENT
            keys[JavaCCConstants.MULTI_LINE_COMMENT] = COMMENT
            // javacc keywords
            keys[JavaCCConstants._OPTIONS] = JAVACC_KEYWORD
            keys[JavaCCConstants._LOOKAHEAD] = JAVACC_KEYWORD
            keys[JavaCCConstants._IGNORE_CASE] = JAVACC_KEYWORD
            keys[JavaCCConstants._PARSER_BEGIN] = JAVACC_KEYWORD
            keys[JavaCCConstants._PARSER_END] = JAVACC_KEYWORD
            keys[JavaCCConstants._JAVACODE] = JAVACC_KEYWORD
            keys[JavaCCConstants._TOKEN] = JAVACC_KEYWORD
            keys[JavaCCConstants._SPECIAL_TOKEN] = JAVACC_KEYWORD
            keys[JavaCCConstants._MORE] = JAVACC_KEYWORD
            keys[JavaCCConstants._SKIP] = JAVACC_KEYWORD
            keys[JavaCCConstants._TOKEN_MGR_DECLS] = JAVACC_KEYWORD
            keys[JavaCCConstants._EOF] = JAVACC_KEYWORD
            // java keywords
            keys[JavaCCConstants.ABSTRACT] = JAVA_KEYWORD
            keys[JavaCCConstants.BOOLEAN] = JAVA_KEYWORD
            keys[JavaCCConstants.BREAK] = JAVA_KEYWORD
            keys[JavaCCConstants.BYTE] = JAVA_KEYWORD
            keys[JavaCCConstants.CASE] = JAVA_KEYWORD
            keys[JavaCCConstants.CATCH] = JAVA_KEYWORD
            keys[JavaCCConstants.CHAR] = JAVA_KEYWORD
            keys[JavaCCConstants.CLASS] = JAVA_KEYWORD
            keys[JavaCCConstants.CONST] = JAVA_KEYWORD
            keys[JavaCCConstants.CONTINUE] = JAVA_KEYWORD
            keys[JavaCCConstants._DEFAULT] = JAVA_KEYWORD
            keys[JavaCCConstants.DO] = JAVA_KEYWORD
            keys[JavaCCConstants.DOUBLE] = JAVA_KEYWORD
            keys[JavaCCConstants.ELSE] = JAVA_KEYWORD
            keys[JavaCCConstants.EXTENDS] = JAVA_KEYWORD
            keys[JavaCCConstants.FALSE] = JAVA_KEYWORD
            keys[JavaCCConstants.FINAL] = JAVA_KEYWORD
            keys[JavaCCConstants.FINALLY] = JAVA_KEYWORD
            keys[JavaCCConstants.FLOAT] = JAVA_KEYWORD
            keys[JavaCCConstants.FOR] = JAVA_KEYWORD
            keys[JavaCCConstants.GOTO] = JAVA_KEYWORD
            keys[JavaCCConstants.IF] = JAVA_KEYWORD
            keys[JavaCCConstants.IMPLEMENTS] = JAVA_KEYWORD
            keys[JavaCCConstants.IMPORT] = JAVA_KEYWORD
            keys[JavaCCConstants.INSTANCEOF] = JAVA_KEYWORD
            keys[JavaCCConstants.INT] = JAVA_KEYWORD
            keys[JavaCCConstants.INTERFACE] = JAVA_KEYWORD
            keys[JavaCCConstants.LONG] = JAVA_KEYWORD
            keys[JavaCCConstants.NATIVE] = JAVA_KEYWORD
            keys[JavaCCConstants.NEW] = JAVA_KEYWORD
            keys[JavaCCConstants.NULL] = JAVA_KEYWORD
            keys[JavaCCConstants.PACKAGE] = JAVA_KEYWORD
            keys[JavaCCConstants.PRIVATE] = JAVA_KEYWORD
            keys[JavaCCConstants.PROTECTED] = JAVA_KEYWORD
            keys[JavaCCConstants.PUBLIC] = JAVA_KEYWORD
            keys[JavaCCConstants.RETURN] = JAVA_KEYWORD
            keys[JavaCCConstants.SHORT] = JAVA_KEYWORD
            keys[JavaCCConstants.STATIC] = JAVA_KEYWORD
            keys[JavaCCConstants.SUPER] = JAVA_KEYWORD
            keys[JavaCCConstants.SWITCH] = JAVA_KEYWORD
            keys[JavaCCConstants.SYNCHRONIZED] = JAVA_KEYWORD
            keys[JavaCCConstants.THIS] = JAVA_KEYWORD
            keys[JavaCCConstants.THROW] = JAVA_KEYWORD
            keys[JavaCCConstants.THROWS] = JAVA_KEYWORD
            keys[JavaCCConstants.TRANSIENT] = JAVA_KEYWORD
            keys[JavaCCConstants.TRUE] = JAVA_KEYWORD
            keys[JavaCCConstants.TRY] = JAVA_KEYWORD
            keys[JavaCCConstants.VOID] = JAVA_KEYWORD
            keys[JavaCCConstants.VOLATILE] = JAVA_KEYWORD
            keys[JavaCCConstants.WHILE] = JAVA_KEYWORD

            // operator
            keys[JavaCCConstants.ASSIGN] = OPERATOR
            keys[JavaCCConstants.GT] = OPERATOR
            keys[JavaCCConstants.LT] = OPERATOR
            keys[JavaCCConstants.BANG] = OPERATOR
            keys[JavaCCConstants.TILDE] = OPERATOR
            keys[JavaCCConstants.HOOK] = OPERATOR
            keys[JavaCCConstants.COLON] = OPERATOR
            keys[JavaCCConstants.EQ] = OPERATOR
            keys[JavaCCConstants.LE] = OPERATOR
            keys[JavaCCConstants.GE] = OPERATOR
            keys[JavaCCConstants.NE] = OPERATOR
            keys[JavaCCConstants.SC_OR] = OPERATOR
            keys[JavaCCConstants.SC_AND] = OPERATOR
            keys[JavaCCConstants.INCR] = OPERATOR
            keys[JavaCCConstants.DECR] = OPERATOR
            keys[JavaCCConstants.PLUS] = OPERATOR
            keys[JavaCCConstants.MINUS] = OPERATOR
            keys[JavaCCConstants.STAR] = OPERATOR
            keys[JavaCCConstants.SLASH] = OPERATOR
            keys[JavaCCConstants.BIT_AND] = OPERATOR
            keys[JavaCCConstants.BIT_OR] = OPERATOR
            keys[JavaCCConstants.XOR] = OPERATOR
            keys[JavaCCConstants.REM] = OPERATOR
            keys[JavaCCConstants.PLUSASSIGN] = OPERATOR
            keys[JavaCCConstants.MINUSASSIGN] = OPERATOR
            keys[JavaCCConstants.STARASSIGN] = OPERATOR
            keys[JavaCCConstants.SLASHASSIGN] = OPERATOR
            keys[JavaCCConstants.ANDASSIGN] = OPERATOR
            keys[JavaCCConstants.ORASSIGN] = OPERATOR
            keys[JavaCCConstants.XORASSIGN] = OPERATOR
            keys[JavaCCConstants.REMASSIGN] = OPERATOR
            keys[JavaCCConstants.SHARP] = OPERATOR

            // semicolon
            keys[JavaCCConstants.SEMICOLON] = OPERATOR
            // parenths
            keys[JavaCCConstants.LPAREN] = PARENTHS
            keys[JavaCCConstants.RPAREN] = PARENTHS
            // brackets
            keys[JavaCCConstants.LBRACKET] = BRACKETS
            keys[JavaCCConstants.RBRACKET] = BRACKETS
            // comma
            keys[JavaCCConstants.COMMA] = COMMA
            // dot
            keys[JavaCCConstants.DOT] = DOT
            // number
            keys[JavaCCConstants.INTEGER_LITERAL] = NUMBER
            keys[JavaCCConstants.FLOATING_POINT_LITERAL] = NUMBER
            // quoted string
            keys[JavaCCConstants.STRING_LITERAL] = STRING
            keys[JavaCCConstants.CHARACTER_LITERAL] = STRING

            // error
            keys[JavaCCConstants.ERROR] = ERROR

            TOKEN_TYPE_TO_STYLE = Collections.unmodifiableMap(keys)
        }

        init {
            val displayNames = HashMap<TextAttributesKey, Pair<String, HighlightSeverity>>()

            displayNames[JAVACC_KEYWORD] = Pair<String, HighlightSeverity>("JavaCC keyword", null)
            displayNames[JAVA_KEYWORD] = Pair<String, HighlightSeverity>("Java keyword", null)
            displayNames[PARENTHS] = Pair<String, HighlightSeverity>("parenths", null)
            displayNames[DOT] = Pair<String, HighlightSeverity>("dot", null)
            displayNames[COMMA] = Pair<String, HighlightSeverity>("comma", null)
            displayNames[BRACKETS] = Pair<String, HighlightSeverity>("brackets", null)
            displayNames[NUMBER] = Pair<String, HighlightSeverity>("number", null)
            displayNames[OPERATOR] = Pair<String, HighlightSeverity>("operator", null)
            displayNames[STRING] = Pair<String, HighlightSeverity>("quoted string", null)
            displayNames[COMMENT] = Pair<String, HighlightSeverity>("comment", null)
            displayNames[ERROR] = Pair<String, HighlightSeverity>("error", null)
            displayNames[TOKEN] = Pair<String, HighlightSeverity>("token", null)

            DISPLAY_NAMES = Collections.unmodifiableMap(displayNames)
        }
    }
}
