package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.intellij.testFramework.ParsingTestCase

/**
 * @author Clément Fournier
 * @since 1.0
 */
class FullParserTest : ParsingTestCase("", "jjt", JavaccParserDefinition) {

    fun testFullGrammar() = doTest(true)

    override fun getTestDataPath(): String = "testData"

    override fun skipSpaces(): Boolean = false

    override fun includeRanges(): Boolean = true
}