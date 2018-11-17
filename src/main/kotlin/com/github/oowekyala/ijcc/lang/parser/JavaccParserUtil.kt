package com.github.oowekyala.ijcc.lang.parser

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
        setJavaLanguageLevel(builder)
        JavaParser.INSTANCE.statementParser.parseCodeBlock(builder)
        return true // FIXME?
    }


    @JvmStatic
    fun parseJCompilationUnit(builder: PsiBuilder, level: Int): Boolean {
        setJavaLanguageLevel(builder)
        JavaParser.INSTANCE.fileParser.parse(builder)
        return true // FIXME?
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

}