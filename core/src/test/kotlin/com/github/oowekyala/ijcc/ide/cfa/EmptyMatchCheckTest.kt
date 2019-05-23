package com.github.oowekyala.ijcc.ide.cfa

import com.github.oowekyala.ijcc.lang.cfa.isEmptyMatchPossible
import com.github.oowekyala.ijcc.lang.cfa.leftMostSet
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.util.JccCoreTestBase
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test

/**
 * @author Clément Fournier
 * @since 1.0
 */
class EmptyMatchCheckTest : JccCoreTestBase() {

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


    @Test
    fun testLookahead() = "LOOKAHEAD(1, Foo())".pos<JccLocalLookaheadUnit>()

    @Test
    fun testOptional() = "[\"f\"]".pos<JccOptionalExpansionUnit>()

    @Test
    fun testAlternativePos() = "\"f\" | [\"f\"]".pos<JccExpansionAlternative>()

    @Test
    fun testAlternativeNeg() = "\"f\" | \"s\"".neg<JccExpansionAlternative>()

    @Test
    fun testParenNeg() = "(\"f\" | \"s\")".neg<JccParenthesizedExpansionUnit>()

    @Test
    fun testParenPos() = "(\"f\" | [\"f\"])".pos<JccParenthesizedExpansionUnit>()

    @Test
    fun testParenPlusPos() = "(\"f\" | [\"f\"])+".pos<JccParenthesizedExpansionUnit>()

    @Test
    fun testParenPlusNeg() = "(\"f\" | \"f\")+".neg<JccParenthesizedExpansionUnit>()

    @Test
    fun testParenOptPos() = "(\"f\" | [\"f\"])?".pos<JccParenthesizedExpansionUnit>()


    @Test
    fun testSeqNeg() = "\"f\" \"f\"".neg<JccExpansionSequence>()

    @Test
    fun testSeqPos() = "[\"f\"] [\"f\"]".pos<JccExpansionSequence>()

    @Test
    fun testReferenceNeg() = "Foo()".neg<JccNonTerminalExpansionUnit>("Foo" to "\"f\"")

    @Test
    fun testReferencePos() = "Foo()".pos<JccNonTerminalExpansionUnit>("Foo" to "[\"f\"]")

    // fixme le parser bug ici
    @Test
    fun testScopedExpansionPos() = "Foo() #Bar".pos<JccExpansionUnit>("Foo" to "[\"f\"]")

    @Test
    fun testScopedExpansionNeg() = "Foo() #Bar".neg<JccExpansionUnit>("Foo" to "\"f\"")

    @Test
    fun testAssignedExpansionPos() = "a=Foo()".pos<JccAssignedExpansionUnit>("Foo" to "[\"f\"]")

    @Test
    fun testAssignedExpansionNeg() = "a=Foo()".neg<JccAssignedExpansionUnit>("Foo" to "\"f\"")


    @Test
    fun testLeftMostSet() {

        val prod = """
            void foo(): {} {
                ("a")? foo()
            }
        """.asProduction() as JccBnfProduction

        prod.leftMostSet()!!.map { it.name }.shouldContainExactly("foo")

    }

    @Test
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
