@file:Suppress("UNUSED_PARAMETER")

package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.lang.JccTypes.*
import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase


/**
 * External BNF rules implementations.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccParserUtil : GeneratedParserUtilBase() {


    /**
     * Parse a Java block. This implementation just eats up
     * to the matching closing brace. Java injection takes
     * the relay.
     *
     * It consumes the insides of a java block, up to and including the
     * closing braces. We assume here an opening brace has already been
     * consumed. This is to preserve the start set of the production in
     * the grammar, to help the generator.
     */
    @JvmStatic
    fun parseJBlock(builder: PsiBuilder, level: Int): Boolean {
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

    /**
     * Parse a the compilation unit between PARSER_(BEGIN|END) tokens.
     * This implementation just eats up to the matching closing brace.
     * Java injection takes the relay for syntax highlighting and stuff.
     */
    @JvmStatic
    fun parseJCompilationUnit(builder: PsiBuilder, level: Int): Boolean {
        // PARSER_BEGIN should have been consumed at the start
        // PARSER_END should be the current token when this method returns, not consumed
        while (builder.tokenType != JCC_PARSER_END_KEYWORD && !builder.eof()) {
            builder.advanceLexer()
        }
        return true
    }

    /**
     * Parse a Java expression.
     */
    @JvmStatic
    fun parseJExpression(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenType == JCC_RPARENTH) return false // when we're in an empty parameter list

        var depth = 1

        while (depth > 0 && !builder.eof()) {
            when (builder.tokenType) {
                JCC_LBRACE, JCC_LPARENTH -> depth++
                JCC_RBRACE, JCC_RPARENTH -> depth--
            }
            if (depth == 0) break
            builder.advanceLexer()
        }

        return true
    }


}
