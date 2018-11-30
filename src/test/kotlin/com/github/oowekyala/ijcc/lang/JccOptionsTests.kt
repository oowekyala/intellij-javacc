package com.github.oowekyala.ijcc.lang

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.model.JccOption
import com.github.oowekyala.ijcc.model.JjtOption
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccOptionsTests : LightCodeInsightFixtureTestCase() {


    override fun getTestDataPath(): String = TestResourcesPath

    fun testDefaultOverride() {
        myFixture.configureByFiles("$OptionsTestDataPath/LookaheadOverride.jjt")
        val file = myFixture.file as JccFile

        // overridden
        file.javaccConfig.lookahead shouldBe 4
    }


    fun testPackageResolution() {
        myFixture.configureByFiles("$OptionsTestDataPath/LookaheadOverride.jjt")
        val file = myFixture.file as JccFile

        file.javaccConfig.parserPackage shouldBe "org.javacc.jjtree"
        file.javaccConfig.nodePackage shouldBe file.javaccConfig.parserPackage
    }

    fun testNodePackageOverride() {
        myFixture.configureByFiles("$OptionsTestDataPath/PackageOverride.jjt")
        val file = myFixture.file as JccFile

        file.javaccConfig.parserPackage shouldBe "org.javacc.jjtree"
        file.javaccConfig.nodePackage shouldBe "org.foo"
    }

    fun testInvalidOptionType() {
        myFixture.configureByFiles("$OptionsTestDataPath/InvalidOptionType.jjt")
        val file = myFixture.file as JccFile

        file.options!!.getOverriddenOptionValue(JjtOption.NODE_DEFAULT_VOID) shouldBe null
        file.options!!.getOverriddenOptionValue(JjtOption.NODE_PACKAGE) shouldBe null
        file.options!!.getOverriddenOptionValue(JccOption.LOOKAHEAD) shouldBe null
    }


}