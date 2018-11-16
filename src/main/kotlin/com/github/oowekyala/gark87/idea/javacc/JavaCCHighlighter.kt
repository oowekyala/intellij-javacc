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

    override fun getHighlightingLexer(): Lexer = com.github.oowekyala.gark87.idea.javacc.generated.JavaCCLexer()

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
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SINGLE_LINE_COMMENT] = COMMENT
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.FORMAL_COMMENT] = COMMENT
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.MULTI_LINE_COMMENT] = COMMENT
            // javacc keywords
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._OPTIONS] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._LOOKAHEAD] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._IGNORE_CASE] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._PARSER_BEGIN] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._PARSER_END] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._JAVACODE] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._TOKEN] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._SPECIAL_TOKEN] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._MORE] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._SKIP] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._TOKEN_MGR_DECLS] = JAVACC_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._EOF] = JAVACC_KEYWORD
            // java keywords
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.ABSTRACT] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.BOOLEAN] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.BREAK] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.BYTE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.CASE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.CATCH] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.CHAR] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.CLASS] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.CONST] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.CONTINUE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants._DEFAULT] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.DO] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.DOUBLE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.ELSE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.EXTENDS] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.FALSE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.FINAL] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.FINALLY] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.FLOAT] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.FOR] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.GOTO] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.IF] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.IMPLEMENTS] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.IMPORT] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.INSTANCEOF] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.INT] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.INTERFACE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.LONG] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.NATIVE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.NEW] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.NULL] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.PACKAGE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.PRIVATE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.PROTECTED] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.PUBLIC] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.RETURN] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SHORT] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.STATIC] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SUPER] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SWITCH] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SYNCHRONIZED] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.THIS] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.THROW] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.THROWS] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.TRANSIENT] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.TRUE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.TRY] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.VOID] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.VOLATILE] = JAVA_KEYWORD
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.WHILE] = JAVA_KEYWORD

            // operator
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.ASSIGN] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.GT] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.LT] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.BANG] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.TILDE] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.HOOK] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.COLON] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.EQ] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.LE] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.GE] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.NE] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SC_OR] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SC_AND] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.INCR] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.DECR] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.PLUS] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.MINUS] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.STAR] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SLASH] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.BIT_AND] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.BIT_OR] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.XOR] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.REM] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.PLUSASSIGN] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.MINUSASSIGN] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.STARASSIGN] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SLASHASSIGN] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.ANDASSIGN] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.ORASSIGN] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.XORASSIGN] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.REMASSIGN] = OPERATOR
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SHARP] = OPERATOR

            // semicolon
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.SEMICOLON] = OPERATOR
            // parenths
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.LPAREN] = PARENTHS
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.RPAREN] = PARENTHS
            // brackets
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.LBRACKET] = BRACKETS
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.RBRACKET] = BRACKETS
            // comma
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.COMMA] = COMMA
            // dot
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.DOT] = DOT
            // number
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.INTEGER_LITERAL] = NUMBER
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.FLOATING_POINT_LITERAL] = NUMBER
            // quoted string
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.STRING_LITERAL] = STRING
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.CHARACTER_LITERAL] = STRING

            // error
            keys[com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants.ERROR] = ERROR

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
