package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.lang.ParserTestDataPath
import com.intellij.lang.java.JavaParserDefinition
import com.intellij.testFramework.ParsingTestCase

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccParserTests : ParsingTestCase("", "jjt", JavaccParserDefinition, JavaParserDefinition()) {

    fun testProductions() = doTest(true)
    fun testTokens() = doTest(true)
    fun testParentheses() = doTest(true)
    fun testLookaheads() = doTest(true)
    fun testRegexPrecedence() = doTest(true)
    fun testAssignments() = doTest(true)
    fun testTokenFail() = doTest(true)

    override fun getTestDataPath(): String = ParserTestDataPath

    override fun skipSpaces(): Boolean = true
}