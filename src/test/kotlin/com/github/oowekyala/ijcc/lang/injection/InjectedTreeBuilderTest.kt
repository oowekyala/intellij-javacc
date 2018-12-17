package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.InjectionTestDataPath
import com.github.oowekyala.ijcc.lang.injection.InjectedTreeBuilderVisitor.Companion.getInjectedSubtreeFor
import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree.*
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.util.*
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import io.kotlintest.matchers.endWith
import io.kotlintest.should
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

    private inline fun <reified N : InjectionStructureTree> matchAsExpansion(ignoreChildren: Boolean = false,
                                                                             noinline nodeSpec: InjectedNodeSpec<N>): AssertionMatcher<String> =
            {
                JccElementFactory.createBnfExpansion(project, it)
                    .let { InjectedTreeBuilderVisitor.getInjectedSubtreeFor(it) }
                    .let {
                        it should matchInjectionTree(ignoreChildren, nodeSpec)
                    }
            }


    fun testExpansionSequences() {

        """ "ff" {jjtThis.foo();} """ should matchAsExpansion<MultiChildNode> {
            child<EmptyLeaf> { }
            child<HostLeaf> { }
        }

    }

    fun testNonterminalExpansions() {

        """ Ola(quetal) """ should matchAsExpansion<SurroundNode> {
            it.prefix shouldBe "Ola("
            it.suffix shouldBe ")"

            child<MultiChildNode> {
                it.delimiter() shouldBe ", "

                child<HostLeaf> {
                    it.host.text shouldBe "quetal"
                }
            }
        }


        """ Ola() """ should matchAsExpansion<SurroundNode> {
            it.prefix shouldBe "Ola()"
            it.suffix shouldBe ""
            child<EmptyLeaf> {}
        }
    }

    fun testBnfs() {

        val (_, linear) = """
            PARSER_BEGIN(foo)
            PARSER_END(foo)

            void MapExpr() #MapExpr(>1) :
            {}
            {
             PathExpr() ( "!" PathExpr() )*
            }

            void PathExpr() #PathExpr(!trivial):
            { boolean trivial = false; }
            {
              RelativePathExpr() { jjtThis.setRelativeAnchor(); }
            }

        """.trimIndent()
            .let { JccElementFactory.createFile(project, it) }
            .let { Pair(getInjectedSubtreeFor(it.grammarFileRoot), it.grammarFileRoot.linearInjectedStructure) }

        stringMatchersIgnoreWhitespace {

            linear.hostSpecs.eachShouldMatchInOrder(
                {
                    it.prefix should endWith("void MapExpr() { ASTMapExpr jjtThis = new ASTMapExpr();")
                    it.host!!.text shouldBe "{}"
                    it.suffix shouldBe null
                },
                {
                    it.prefix shouldBe "PathExpr()/*seq*/ while (/* +* */i6()) {/*seq*/ PathExpr()} jjtree.closeNodeScope(jjtThis, jjtree.arity() >"
                    it.host!!.text shouldBe "1"
                    it.suffix shouldBe "); }"
                },
                {
                    it.prefix shouldBe "void PathExpr() { ASTPathExpr jjtThis = new ASTPathExpr();"
                    it.host!!.text shouldBe "{ boolean trivial = false; }"
                    it.suffix shouldBe null
                },
                {
                    it.prefix shouldBe "RelativePathExpr()/*seq*/"
                    it.host!!.text shouldBe "{ jjtThis.setRelativeAnchor(); }"
                    it.suffix shouldBe null
                },
                {
                    it.prefix shouldBe "jjtree.closeNodeScope(jjtThis,"
                    it.host!!.text shouldBe "!trivial"
                    it.suffix shouldBe "); }}" // the last } closes the class
                }
            )
        }
    }

}