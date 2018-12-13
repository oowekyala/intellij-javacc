package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.InjectionTestDataPath
import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree.*
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.util.NWrapper
import com.github.oowekyala.ijcc.lang.util.matchInjectionTree
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.should

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

    private inline fun <reified N : InjectionStructureTree> matchExpansionTree(ignoreChildren: Boolean = false,
                                                                               noinline nodeSpec: NWrapper<InjectionStructureTree, N>.() -> Unit): Matcher<String> =
            object : Matcher<String> {
                override fun test(value: String): Result =
                        JccElementFactory.createBnfExpansion(project, value)
                            .let { InjectedTreeBuilderVisitor.getSubtreeFor(it) }
                            .let {
                                matchInjectionTree(ignoreChildren, nodeSpec).test(it)
                            }
            }


    fun testSimpleBnf() {

        """ "ff" {jjtThis.foo();} """ should matchExpansionTree<MultiChildNode> {
            child<EmptyLeaf> { }
            child<HostLeaf> { }
        }

    }

}