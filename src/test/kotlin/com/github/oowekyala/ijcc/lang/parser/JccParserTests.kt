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

    private val checkIt = false

    fun testProductions() = doTest(checkIt)
    fun testTokens() = doTest(checkIt)
    fun testParentheses() = doTest(checkIt)
    fun testLookaheads() = doTest(checkIt)
    fun testRegexPrecedence() = doTest(checkIt)
    fun testAssignments() = doTest(checkIt)
    fun testTokenFail() = doTest(checkIt)

    override fun getTestDataPath(): String = ParserTestDataPath

    override fun skipSpaces(): Boolean = true
}