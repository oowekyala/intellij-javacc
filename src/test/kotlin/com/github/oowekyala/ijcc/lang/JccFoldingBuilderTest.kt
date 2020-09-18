package com.github.oowekyala.ijcc.lang

import com.github.oowekyala.ijcc.lang.util.JccTestBase

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccFoldingBuilderTest : JccTestBase() {


    override fun getTestDataPath(): String = TestResourcesPath

    fun testFolding() {
        myFixture.configureByFiles()
        myFixture.testFoldingWithCollapseStatus("$FoldingTestDataPath/ParserActions.jjt")
    }

    fun testFoldingJjtreeGen() {
        myFixture.configureByFiles()
        myFixture.testFoldingWithCollapseStatus("$FoldingTestDataPath/JjtreeGen.jj")
    }

}
