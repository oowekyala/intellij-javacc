package com.github.oowekyala.ijcc.insight.cfa

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.JccAnnotationTestBase
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class EmptyMatchCheckTest : JccAnnotationTestBase() {

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


    fun testLookahead() = "LOOKAHEAD(1, Foo())".pos<JccLocalLookahead>()
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

    fun testScopedExpansionPos() = "Foo() #Bar".pos<JccScopedExpansionUnit>("Foo" to "[\"f\"]")
    fun testScopedExpansionNeg() = "Foo() #Bar".neg<JccScopedExpansionUnit>("Foo" to "\"f\"")

    fun testAssignedExpansionPos() = "a=Foo()".pos<JccAssignedExpansionUnit>("Foo" to "[\"f\"]")
    fun testAssignedExpansionNeg() = "a=Foo()".neg<JccAssignedExpansionUnit>("Foo" to "\"f\"")


}