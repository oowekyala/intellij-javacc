package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.ide.inspections.ActionWithinLookaheadInspection.Companion.ProblemDescription

/**
 * @author Clément Fournier
 * @since 1.0
 */
class ActionsWithinLookaheadInspectionTest : JccInspectionTestBase(ActionWithinLookaheadInspection()) {

    private fun ignored(s: String) = warningAnnot(s, ProblemDescription)

    fun testPos() = checkByText(
        """
           LOOKAHEAD(4,Foo() ${ignored("{}")})
    """.inExpansionCtx("Foo")
    )

    fun testNeg() = checkByText(
        """
          LOOKAHEAD(4,Foo() ${ignored("{}")}) {}
    """.inExpansionCtx("Foo")
    )

    fun testNestedSyntactic() = checkByText(
        """
           (LOOKAHEAD(1, "f" | LOOKAHEAD(4,Foo()${ignored("{}")}) Foo()) "foo")?
    """.inExpansionCtx("Foo")
    )

}