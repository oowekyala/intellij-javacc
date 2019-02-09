package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.ide.inspections.RegexMayMatchEmptyStringInspection.Companion.makeMessage
import com.github.oowekyala.ijcc.lang.model.LexicalState

/**
 * @author Clément Fournier
 * @since 1.1
 */
class RegexMayMatchEmptyStringInspectionTest : JccInspectionTestBase(RegexMayMatchEmptyStringInspection()) {

    private fun String.warning(name: String? = "FOO",
                               states: List<String> = LexicalState.JustDefaultState) =
            warningAnnot(trimIndent(), makeMessage(name, states))


    fun testEasyNoName() = checkByText(
        """
            $DummyHeader

            TOKEN: {
                ${"\"\"".warning(null)}
            }
        """
    )


    fun testEasyWithName() = checkByText(
        """
            $DummyHeader

            TOKEN: {
                <FOO: ${"\"\"".warning()}>
            }
        """
    )

    fun testWithAlternative() = checkByText(
        """
            $DummyHeader

            TOKEN: {
                <FOO:${"\"a\" | (\"b\")?".warning()}>
            }
        """
    )


    fun testWithRef() = checkByText(
        """
            $DummyHeader
            TOKEN:{
                  <FOO: ${"(<BAZ>)?".warning()}>
                | <BAR: <BAZ>>
                | <BAZ: "foo">
            }

        """
    )


}