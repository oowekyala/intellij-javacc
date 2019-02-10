package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.ide.inspections.LookaheadIsNotAtChoicePointInspection.Companion.IgnoredProblemDesc
import com.github.oowekyala.ijcc.ide.inspections.LookaheadIsNotAtChoicePointInspection.Companion.NestedProblemDesc
import com.github.oowekyala.ijcc.ide.inspections.LookaheadIsNotAtChoicePointInspection.Companion.SemanticProblemDesc

/**
 * @author Clément Fournier
 * @since 1.0
 */
class LookaheadIsNotAtChoicePointTest : JccInspectionTestBase(LookaheadIsNotAtChoicePointInspection()) {

    private fun ignored(s: String) = warningAnnot(s, IgnoredProblemDesc)
    private fun semantic(s: String) = warningAnnot(s, SemanticProblemDesc)
    private fun nested(s: String) = warningAnnot(s, NestedProblemDesc)

    fun testLoneLookahead() = checkByText(
        """
           ${ignored("LOOKAHEAD(1)")}
    """.inExpansionCtx()
    )

    fun testInSequence() = checkByText(
        """
           ${ignored("LOOKAHEAD(1)")} "foo" "bar"
    """.inExpansionCtx()
    )

    fun testInAlternative() = checkByText(
        """
           LOOKAHEAD(1) "foo" |  "bar"
    """.inExpansionCtx()
    )

    fun testInOneOrMore() = checkByText(
        """
           (LOOKAHEAD(1) "foo")+
    """.inExpansionCtx()
    )

    fun testInZeroOrMore() = checkByText(
        """
           (LOOKAHEAD(1) "foo")*
    """.inExpansionCtx()
    )


    fun testInMiddleOfSeqZeroOrMore() = checkByText(
        """
           ("f" ${ignored("LOOKAHEAD(1)")} "foo")*
    """.inExpansionCtx()
    )

    fun testInZeroOrOne() = checkByText(
        """
           (LOOKAHEAD(1) "foo")?
    """.inExpansionCtx()
    )

    fun testInOptional() = checkByText(
        """
           [LOOKAHEAD(1) "foo"]
    """.inExpansionCtx()
    )

    fun testInRegularParenthesized() = checkByText(
        """
           (${ignored("LOOKAHEAD(1)")} "foo")
    """.inExpansionCtx()
    )

    fun testOnlySemantic() = checkByText(
        """
           (${semantic("LOOKAHEAD(1, Foo(), {hey()})")} "foo")
    """.inExpansionCtx("Foo")
    )

    fun testNoSemantic() = checkByText(
        """
           (${ignored("LOOKAHEAD(1, Foo())")} "foo")
    """.inExpansionCtx("Foo")
    )

    fun testNestedIgnored() = checkByText(
        """
           (LOOKAHEAD(1, ${ignored("LOOKAHEAD(4,Foo())")} Foo()) "foo")?
    """.inExpansionCtx("Foo")
    )


    fun testNestedSyntactic() = checkByText(
        """
           (LOOKAHEAD(1, "f" | ${nested("LOOKAHEAD(4,Foo())")} Foo()) "foo")?
    """.inExpansionCtx("Foo")
    )

}