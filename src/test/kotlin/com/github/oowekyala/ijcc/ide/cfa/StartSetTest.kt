package com.github.oowekyala.ijcc.ide.cfa

import com.github.oowekyala.ijcc.lang.cfa.*
import com.github.oowekyala.ijcc.lang.psi.JccBnfProduction
import com.github.oowekyala.ijcc.lang.shouldBeA
import com.github.oowekyala.ijcc.lang.shouldContainOneSuch
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class StartSetTest : JccTestBase() {

    private fun String.test(vararg otherProdNamesAndExps: Pair<String, String>,
                            groupAtomic: Boolean = false,
                            test: Set<AtomicUnit>.() -> Unit) {
        val r = asExpansion(*otherProdNamesAndExps)

        r.firstSet(groupAtomic).test()
    }



    fun testLookahead() = "LOOKAHEAD(1, Foo())".test {
        this shouldBe emptySet()
    }


    fun testOptional() = "[\"f\"]".test {
        this.shouldNotBeEmpty()
        this.first().shouldBeA<AtomicToken> {
            it.token.asStringToken shouldNotBe null
            it.token.asStringToken!!.text shouldBe "\"f\""
        }
    }


    fun testAlternativePos() = "\"f\" | [\"a\"]".test {
        this.shouldNotBeEmpty()
        this.shouldContainOneSuch {
            it.shouldBeA<AtomicToken> {
                it.token.asStringToken shouldNotBe null
                it.token.asStringToken!!.text shouldBe "\"f\""
            }
        }
        this.shouldContainOneSuch {
            it.shouldBeA<AtomicToken> {
                it.token.asStringToken shouldNotBe null
                it.token.asStringToken!!.text shouldBe "\"a\""
            }
        }
    }



    fun testSeqNeg() = "\"f\" \"b\"".test {
        this should haveSize(1)
        this.shouldContainOneSuch {
            it.shouldBeA<AtomicToken> {
                it.token.asStringToken shouldNotBe null
                it.token.asStringToken!!.text shouldBe "\"f\""
            }
        }
    }


    fun testSeqPos() = "[\"f\"] [\"a\"]".test {
        this should haveSize(2)
        this.shouldContainOneSuch {
            it.shouldBeA<AtomicToken> {
                it.token.asStringToken shouldNotBe null
                it.token.asStringToken!!.text shouldBe "\"f\""
            }
        }
        this.shouldContainOneSuch {
            it.shouldBeA<AtomicToken> {
                it.token.asStringToken shouldNotBe null
                it.token.asStringToken!!.text shouldBe "\"a\""
            }
        }
    }


    fun testReferenceNeg() = "Foo()".test("Foo" to "\"f\"") {
        this should haveSize(1)
        this.shouldContainOneSuch {
            it.shouldBeA<AtomicToken> {
                it.token.asStringToken shouldNotBe null
                it.token.asStringToken!!.text shouldBe "\"f\""
            }
        }
    }


    fun testReferenceAtomic() = "Foo()".test(
        "Foo" to "\"f\"",
        groupAtomic = true
    ) {
        this should haveSize(1)
        this.shouldContainOneSuch {
            it.shouldBeA<AtomicProduction> {
                it.production.name shouldBe "Foo"
            }
        }
    }


    fun testReferenceAtomic2() = "Foo()".test(
        "Foo" to "\"f\" | Bar()",
        "Bar" to "\"g\"",
        groupAtomic = true
    ) {
        this should haveSize(1)
        this.shouldContainOneSuch {
            it.shouldBeA<AtomicProduction> {
                it.production.name shouldBe "Foo"
            }
        }
    }


    fun testReferencePos() = "Foo()".test("Foo" to "[\"f\"]") {
        this should haveSize(1)
        this.shouldContainOneSuch {
            it.shouldBeA<AtomicToken> {
                it.token.asStringToken shouldNotBe null
                it.token.asStringToken!!.text shouldBe "\"f\""
            }
        }
    }


    fun testUnresolvedProd() = "Foo() #Bar".test {
        this should haveSize(1)
        this.shouldContainOneSuch {
            it.shouldBeA<AtomicUnresolvedProd> {
                it.ref.name shouldBe "Foo"
            }
        }
    }


    fun testScopedExpansionNeg() = "Foo() #Bar".test("Foo" to "\"f\"") {
    }


    fun testAssignedExpansionPos() = "a=Foo()".test("Foo" to "[\"f\"]") {
    }


    fun testAssignedExpansionNeg() = "a=Foo()".test("Foo" to "\"f\"") {
    }


    fun testLeftMostSet() {

        val prod = """
            void foo(): {} {
                ("a")? foo()
            }
        """.asProduction() as JccBnfProduction

        prod.leftMostSet()!!.map { it.name }.shouldContainExactly("foo")

    }


    fun testLeftMostSetAlt() {

        val prod = """
            void foo(): {} {
                ("a")? foo() | bar() qux() quux()
            }

            void bar(): {} {
              ("f")?
            }

            void qux(): {} {
              "f"
            }

            void quux(): {} {
              bar()
            }

        """.asJccGrammar().nonTerminalProductions.first()

        prod.leftMostSet()!!.map { it.name }.shouldContainExactly("foo", "bar", "qux")

    }

}
