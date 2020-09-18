package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.lang.ParserTestDataPath
import com.intellij.testFramework.ParsingTestCase

/**
 * @author Clément Fournier
 * @since 1.6
 */
class Jcc21ParserTests : ParsingTestCase("21", "javacc", JavaccParserDefinition) {

    private val checkIt = true

    // TODO the end is wrong
    fun testSimple() = doTest(checkIt)

    override fun getTestDataPath(): String = ParserTestDataPath

    override fun skipSpaces(): Boolean = true
}
