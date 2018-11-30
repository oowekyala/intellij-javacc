package com.github.oowekyala.ijcc.lang

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccCodeInsightTests : LightCodeInsightFixtureTestCase() {


    override fun getTestDataPath(): String = TestResourcesPath

    fun testFolding() {
        myFixture.configureByFiles()
        myFixture.testFoldingWithCollapseStatus("$FoldingTestDataPath/ParserActions.jjt")
    }

    companion object {
        private const val Fixtures = "fixtures"
        val FoldingTestDataPath = TestResourcesPath + JccCodeInsightTests::class.dataPath("folding", Fixtures)

    }
}