package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.InjectionTestDataPath
import com.github.oowekyala.ijcc.lang.psi.JccBnfProduction
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class InjectedTreeBuilderTest : LightCodeInsightFixtureTestCase() {


    private var commonFileImpl: JccFile? = null
    private val commonFile: JccFile
        get() = commonFileImpl!!

    override fun setUp() {
        super.setUp()
        myFixture.configureByFiles("$InjectionTestDataPath/CommonTreeBuilderTest.jjt")
        commonFileImpl = myFixture.file as JccFile
    }

    fun testSimpleBnf() {

        /*
        void test1() #OptionBinding :
        {}
        {
          "ff" {jjtThis.foo();}
        }
         */

        val bnf = commonFile.nonTerminalProductions.first { it.name == "test1" } as JccBnfProduction

        val bnfStack = InjectedTreeBuilderVisitor().also { it.visitBnfProduction(bnf) }.nodeStack

        bnfStack.size shouldBe 1
    }

}