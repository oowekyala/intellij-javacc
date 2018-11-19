package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.lang.JavaccTypes.*
import com.intellij.lang.PsiBuilder
import com.intellij.lang.java.parser.JavaParser
import com.intellij.lang.java.parser.JavaParserUtil
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.JavaTokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.java.IJavaElementType


/**
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccParserUtil : GeneratedParserUtilBase() {

    @JvmStatic
    fun parseJBlock(builder: PsiBuilder, level: Int): Boolean {

        val inBlock = consumeToken(builder, JCC_LBRACE)
        if (!inBlock) return false

        var depth = 1

        while (depth > 0 && !builder.eof()) {
            when (builder.tokenType) {
                JCC_LBRACE -> depth++
                JCC_RBRACE -> depth--
            }
            builder.advanceLexer()
        }

        return true
    }


    @JvmStatic
    fun parseJCompilationUnit(builder: PsiBuilder, level: Int): Boolean {
        // PARSER_END should be the current token when this method returns
        while (builder.tokenType != JCC_PARSER_END_KEYWORD && !builder.eof()) {
            builder.advanceLexer()
        }
        return true
    }

    private fun PsiBuilder.javaContext(
        remapper: TokenTypeRemapper = BaseJavaRemapper,
        block: (PsiBuilder) -> Boolean
    ): Boolean {
        JavaParserUtil.setLanguageLevel(this, LanguageLevel.JDK_1_4)
        return block(RemappedBuilder(this, remapper))
    }

    @JvmStatic
    fun parseJExpression(builder: PsiBuilder, level: Int): Boolean =
        builder.javaContext {
            JavaParser.INSTANCE.expressionParser.parse(it)
            true
        }

    @JvmStatic
    fun parseJAssignmentLhs(builder: PsiBuilder, level: Int): Boolean =
        builder.javaContext(JavaRemapperNoAssignment) {
            JavaParser.INSTANCE.expressionParser.parse(it)
            it.tokenType == JavaTokenType.EQ // Only say ok if the next token is an EQ
        }

    private class AcuPsiBuilderDelegate(val base: PsiBuilder) : PsiBuilder by base {

        // Stops on PARSER_END
        override fun eof(): Boolean = tokenType == JCC_PARSER_END_KEYWORD || base.eof()

    }

    interface TokenTypeRemapper {
        fun remap(source: IElementType?, text: () -> String?): IElementType?
    }


    /**
     * Maps Javacc tokens to Java tokens so that java parsers may use the PsiBuilder.
     *
     * The built-in remap mechanism of PsiBuilderImpl doesn't remap tokens when we backtrack.
     */
    private class RemappedBuilder(private val psiBuilder: PsiBuilder, private val remapper: TokenTypeRemapper) :
        PsiBuilder by psiBuilder {

        override fun getTokenType(): IElementType? = remapper.remap(psiBuilder.tokenType, this::getTokenText)

    }


    private object JavaRemapperNoAssignment : TokenTypeRemapper {
        override fun remap(source: IElementType?, text: () -> String?): IElementType? {
            val fstRemap = BaseJavaRemapper.remap(source, text)
            return when (fstRemap) {
                JavaTokenType.EQ -> null // EOF
                else             -> fstRemap
            }
        }
    }

    private object BaseJavaRemapper : TokenTypeRemapper {
        override fun remap(source: IElementType?, text: () -> String?): IElementType? {
            return when (source) {
                is IJavaElementType         -> source
                JCC_STRING_LITERAL          -> JavaTokenType.STRING_LITERAL
                JCC_IDENT                   -> JavaTokenType.IDENTIFIER

                JCC_LBRACE                  -> JavaTokenType.LBRACE
                JCC_LBRACKET                -> JavaTokenType.LBRACKET
                JCC_LPARENTH                -> JavaTokenType.LPARENTH

                JCC_RBRACE                  -> JavaTokenType.RBRACE
                JCC_RBRACKET                -> JavaTokenType.RBRACKET
                JCC_RPARENTH                -> JavaTokenType.RPARENTH

                JCC_EQ                      -> JavaTokenType.EQ

                JCC_ASTERISK                -> JavaTokenType.ASTERISK
                JCC_BAD_CHARACTER           -> JavaTokenType.BAD_CHARACTER
                JCC_CATCH_KEYWORD           -> JavaTokenType.CATCH_KEYWORD
                JCC_CHARACTER_LITERAL       -> JavaTokenType.CHARACTER_LITERAL
                JCC_COLON                   -> JavaTokenType.COLON
                JCC_COMMA                   -> JavaTokenType.COMMA
                JCC_C_STYLE_COMMENT         -> JavaTokenType.C_STYLE_COMMENT
                JCC_DOC_COMMENT             -> JavaTokenType.C_STYLE_COMMENT
                JCC_DOT                     -> JavaTokenType.DOT
                JCC_DOUBLE_LITERAL          -> JavaTokenType.DOUBLE_LITERAL
                JCC_ELLIPSIS                -> JavaTokenType.ELLIPSIS
                JCC_END_OF_LINE_COMMENT     -> JavaTokenType.END_OF_LINE_COMMENT
                JCC_FALSE_KEYWORD           -> JavaTokenType.FALSE_KEYWORD
                JCC_FINALLY_KEYWORD         -> JavaTokenType.FINALLY_KEYWORD
                JCC_FLOAT_LITERAL           -> JavaTokenType.FLOAT_LITERAL
                JCC_GT                      -> JavaTokenType.GT

                // These javacc keywords are mapped to identifiers
                // in fact they should cause an unexpected token error
                JCC_EOF_KEYWORD             -> JavaTokenType.IDENTIFIER
                JCC_IGNORE_CASE_OPTION      -> JavaTokenType.IDENTIFIER
                JCC_JAVACODE_KEYWORD        -> JavaTokenType.IDENTIFIER
                JCC_LOOKAHEAD_KEYWORD       -> JavaTokenType.IDENTIFIER
                JCC_MORE_KEYWORD            -> JavaTokenType.IDENTIFIER
                JCC_PARSER_BEGIN_KEYWORD    -> JavaTokenType.IDENTIFIER
                JCC_PARSER_END_KEYWORD      -> JavaTokenType.IDENTIFIER
                JCC_SKIP_KEYWORD            -> JavaTokenType.IDENTIFIER
                JCC_SPECIAL_TOKEN_KEYWORD   -> JavaTokenType.IDENTIFIER
                JCC_TOKEN_KEYWORD           -> JavaTokenType.IDENTIFIER
                JCC_TOKEN_MGR_DECLS_KEYWORD -> JavaTokenType.IDENTIFIER

                JCC_POUND                   -> JCC_POUND // not remapped, not a java token

                JCC_INTEGER_LITERAL         -> JavaTokenType.INTEGER_LITERAL
                JCC_LONG_LITERAL            -> JavaTokenType.LONG_LITERAL
                JCC_LT                      -> JavaTokenType.LT
                JCC_MINUS                   -> JavaTokenType.MINUS
                JCC_PLUS                    -> JavaTokenType.PLUS
                JCC_PRIMITIVE_TYPE          ->
                    when (text()) {
                        "int"     -> JavaTokenType.INT_KEYWORD
                        "short"   -> JavaTokenType.SHORT_KEYWORD
                        "boolean" -> JavaTokenType.BOOLEAN_KEYWORD
                        "byte"    -> JavaTokenType.BYTE_KEYWORD
                        "long"    -> JavaTokenType.LONG_KEYWORD
                        "char"    -> JavaTokenType.CHAR_KEYWORD
                        "float"   -> JavaTokenType.FLOAT_KEYWORD
                        "double"  -> JavaTokenType.DOUBLE_KEYWORD
                        else      -> throw IllegalStateException("Unhandled primitive type")
                    }
                JCC_PRIVATE_KEYWORD         -> JavaTokenType.PRIVATE_KEYWORD
                JCC_PROTECTED_KEYWORD       -> JavaTokenType.PROTECTED_KEYWORD
                JCC_PUBLIC_KEYWORD          -> JavaTokenType.PUBLIC_KEYWORD
                JCC_QUESTION                -> JavaTokenType.QUEST
                JCC_SEMICOLON               -> JavaTokenType.SEMICOLON
                JCC_STATIC_KEYWORD          -> JavaTokenType.STATIC_KEYWORD
                JCC_THROWS_KEYWORD          -> JavaTokenType.THROWS_KEYWORD
                JCC_TILDE                   -> JavaTokenType.TILDE
                JCC_TRUE_KEYWORD            -> JavaTokenType.TRUE_KEYWORD
                JCC_TRY_KEYWORD             -> JavaTokenType.TRY_KEYWORD
                JCC_UNION                   -> JavaTokenType.OR
                JCC_VOID_KEYWORD            -> JavaTokenType.VOID_KEYWORD
                else                        -> source
            }
        }
    }

}