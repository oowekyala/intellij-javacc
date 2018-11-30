package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.lang.TestResourcesPath
import com.github.oowekyala.ijcc.lang.dataPath
import com.intellij.testFramework.ParsingTestCase

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccParserTests : ParsingTestCase(JccParserTests::class.dataPath(), "jjt", JavaccParserDefinition) {

    fun testProductions() = doTest(true)
    fun testTokens() = doTest(true)

    override fun getTestDataPath(): String = TestResourcesPath

    override fun skipSpaces(): Boolean = false
}