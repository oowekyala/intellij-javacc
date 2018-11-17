package com.github.oowekyala.ijcc.lang.parser

import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.lang.PsiBuilder
import com.intellij.lang.java.JavaParserDefinition
import com.intellij.lang.java.lexer.JavaLexer
import com.intellij.lang.java.parser.JavaParser
import com.intellij.pom.java.LanguageLevel


/**
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccParserUtil : GeneratedParserUtilBase() {

    @JvmStatic
    fun parseJBlock(builder: PsiBuilder, level: Int): Boolean {
        JavaParser.INSTANCE.statementParser.parseCodeBlock(builder)
        return true // FIXME?
    }


    @JvmStatic
    fun parseJCompilationUnit(builder: PsiBuilder, level: Int): Boolean {
        JavaParser.INSTANCE.fileParser.parse(builder)
        return true // FIXME?
    }



}