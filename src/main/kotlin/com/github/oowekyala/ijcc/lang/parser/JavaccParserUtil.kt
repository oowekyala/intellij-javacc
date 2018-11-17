package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.intellij.lang.PsiBuilder
import com.intellij.lang.java.parser.JavaParser
import com.intellij.lang.java.parser.JavaParserUtil
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.pom.java.LanguageLevel


/**
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccParserUtil : GeneratedParserUtilBase() {

    private fun setJavaLanguageLevel(builder: PsiBuilder) {
        JavaParserUtil.setLanguageLevel(builder, LanguageLevel.JDK_1_4)
    }

    @JvmStatic
    fun parseJBlock(builder: PsiBuilder, level: Int): Boolean {

        val inBlock = consumeToken(builder, JavaccTypes.JCC_LBRACE)
        if (!inBlock) return false

        var depth = 1

        while (depth > 0 && !builder.eof()) {
            when (builder.tokenType) {
                JavaccTypes.JCC_LBRACE -> depth++
                JavaccTypes.JCC_RBRACE -> depth--
            }
            builder.advanceLexer()
        }

        return true
    }


    @JvmStatic
    fun parseJCompilationUnit(builder: PsiBuilder, level: Int): Boolean {
        setJavaLanguageLevel(builder)
        JavaParser.INSTANCE.fileParser.parse(AcuPsiBuilderDelegate(builder))
        return true
    }


    @JvmStatic
    fun parseJExpression(builder: PsiBuilder, level: Int): Boolean {
        setJavaLanguageLevel(builder)
        JavaParser.INSTANCE.expressionParser.parse(builder)
        return true // FIXME?
    }

    // FIXME!!
    @JvmStatic
    fun parseJAssignmentLhs(builder: PsiBuilder, level: Int): Boolean {
        setJavaLanguageLevel(builder)
        JavaParser.INSTANCE.expressionParser.parse(builder)
        return true // FIXME?
    }

    private class AcuPsiBuilderDelegate(val base: PsiBuilder) : PsiBuilder by base {

        // Stops on PARSER_END
        override fun eof(): Boolean = tokenType == JavaccTypes.JCC_PARSER_END_KEYWORD || base.eof()

    }

}