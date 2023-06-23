package com.github.oowekyala.ijcc.ide.cfa

import com.github.oowekyala.ijcc.lang.cfa.isEmptyMatchPossible
import com.github.oowekyala.ijcc.lang.cfa.leftMostSet
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class EmptyMatchCheckTest : JccTestBase() {

    private inline fun <reified R : JccExpansion> String.test(isPos: Boolean,
                                                              vararg otherProdNamesAndExps: Pair<String, String>) {
        val r = asExpansion(*otherProdNamesAndExps).also { check(it is R) } as R

        r.isEmptyMatchPossible() shouldBe isPos
    }


    private inline fun <reified R : JccExpansion> String.neg(vararg otherProdNamesAndExps: Pair<String, String>) {
        test<R>(false, *otherProdNamesAndExps)
    }

    private inline fun <reified R : JccExpansion> String.pos(vararg otherProdNamesAndExps: Pair<String, String>) {
        test<R>(true, *otherProdNamesAndExps)
    }


    fun testLookahead() = "LOOKAHEAD(1, Foo())".pos<JccLocalLookaheadUnit>()

    fun testOptional() = "[\"f\"]".pos<JccOptionalExpansionUnit>()

    fun testAlternativePos() = "\"f\" | [\"f\"]".pos<JccExpansionAlternative>()

    fun testAlternativeNeg() = "\"f\" | \"s\"".neg<JccExpansionAlternative>()

    fun testParenNeg() = "(\"f\" | \"s\")".neg<JccParenthesizedExpansionUnit>()

    fun testParenPos() = "(\"f\" | [\"f\"])".pos<JccParenthesizedExpansionUnit>()

    fun testParenPlusPos() = "(\"f\" | [\"f\"])+".pos<JccParenthesizedExpansionUnit>()

    fun testParenPlusNeg() = "(\"f\" | \"f\")+".neg<JccParenthesizedExpansionUnit>()

    fun testParenOptPos() = "(\"f\" | [\"f\"])?".pos<JccParenthesizedExpansionUnit>()


    fun testSeqNeg() = "\"f\" \"f\"".neg<JccExpansionSequence>()

    fun testSeqPos() = "[\"f\"] [\"f\"]".pos<JccExpansionSequence>()

    fun testReferenceNeg() = "Foo()".neg<JccNonTerminalExpansionUnit>("Foo" to "\"f\"")

    fun testReferencePos() = "Foo()".pos<JccNonTerminalExpansionUnit>("Foo" to "[\"f\"]")

    // fixme le parser bug ici
    fun testScopedExpansionPos() = "Foo() #Bar".pos<JccScopedExpansionUnit>("Foo" to "[\"f\"]")

    fun testScopedExpansionNeg() = "Foo() #Bar".neg<JccScopedExpansionUnit>("Foo" to "\"f\"")

    fun testAssignedExpansionPos() = "a=Foo()".pos<JccAssignedExpansionUnit>("Foo" to "[\"f\"]")

    fun testAssignedExpansionNeg() = "a=Foo()".neg<JccAssignedExpansionUnit>("Foo" to "\"f\"")


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
