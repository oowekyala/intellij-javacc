package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.injection.InjectedTreeBuilderVisitor.Companion.getInjectedSubtreeFor
import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree.*
import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.lang.util.*
import io.kotlintest.matchers.endWith
import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.should
import io.kotlintest.shouldBe
import org.junit.Test

/**
 * @author Clément Fournier
 * @since 1.0
 */
class InjectedTreeBuilderTest : JccCoreTestBase() {


    private inline fun <reified N : InjectionStructureTree> matchAsExpansion(ignoreChildren: Boolean = false,
                                                                             noinline nodeSpec: InjectedNodeSpec<N>): AssertionMatcher<String> =
        {
            project.jccEltFactory.createExpansion<JccExpansion>(it)
                .let { getInjectedSubtreeFor(it) }
                .let {
                    it should matchInjectionTree(ignoreChildren, nodeSpec)
                }
        }


    @Test
    fun testExpansionSequences() {

        """ "ff" {jjtThis.foo();} """ should matchAsExpansion<MultiChildNode> {
            child<EmptyLeaf> { }
            child<HostLeaf> { }
        }

    }

    @Test
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

    @Test
    fun testBnfs() {

        val (tree, linear) = """
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
            .let { project.jccEltFactory.createFile(it) }
            .let {
                Pair(
                    getInjectedSubtreeFor(it.grammarFileRoot!!),
                    it.grammarFileRoot!!.linearInjectedStructure
                )
            }

        stringMatchersIgnoreWhitespace {

            tree should matchInjectionTree<SurroundNode> {
                it.prefix.shouldStartWith("class MyParser {")

                it.child shouldBe child<MultiChildNode> {
                    child<StructuralBoundary> {

                        it.child shouldBe child<SurroundNode> {
                            it.prefix shouldBe "void MapExpr() { ASTMapExpr jjtThis = new ASTMapExpr();"

                            it.child shouldBe child<MultiChildNode> {
                                child<HostLeaf> {}
                                child<MultiChildNode> {
                                    textLeaf("PathExpr()")
                                    child<SurroundNode> {
                                        it.prefix shouldBe "while (/* +* */i0()) {"

                                        it.child shouldBe child<MultiChildNode> {
                                            child<EmptyLeaf> {}
                                            textLeaf("PathExpr()")
                                        }
                                        it.suffix shouldBe "}"
                                    }
                                }
                                child<SurroundNode> {
                                    it.prefix shouldBe "jjtree.closeNodeScope(jjtThis, "

                                    it.child shouldBe child<SurroundNode> {
                                        it.prefix shouldBe "jjtree.arity() > "
                                        it.suffix shouldBe ""

                                        it.child shouldBe child<HostLeaf> {}
                                    }
                                    it.suffix shouldBe ");"
                                }
                            }
                            it.suffix shouldBe "}"
                        }

                    }
                    child<StructuralBoundary> {

                        it.child shouldBe child<SurroundNode> {
                            it.prefix shouldBe "void PathExpr() { ASTPathExpr jjtThis = new ASTPathExpr();"
                            it.suffix shouldBe "}"

                            it.child shouldBe child<MultiChildNode> {
                                child<HostLeaf> {}
                                child<MultiChildNode> {
                                    textLeaf("RelativePathExpr()")
                                    child<HostLeaf> {}
                                }
                                child<SurroundNode> {
                                    it.prefix shouldBe "jjtree.closeNodeScope(jjtThis, "
                                    it.suffix shouldBe ");"

                                    it.child shouldBe child<HostLeaf> {}
                                }
                            }
                        }
                    }
                }
                it.suffix shouldBe "}"
            }

            linear.hostSpecs.eachShouldMatchInOrder(
                {
                    it.prefix should endWith("void MapExpr() { ASTMapExpr jjtThis = new ASTMapExpr();")
                    it.host!!.text shouldBe "{}"
                    it.suffix shouldBe null
                },
                {
                    it.prefix shouldBe "PathExpr()/*seq*/ while (/* +* */i0()) {/*seq*/ PathExpr()} jjtree.closeNodeScope(jjtThis, jjtree.arity() >"
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
