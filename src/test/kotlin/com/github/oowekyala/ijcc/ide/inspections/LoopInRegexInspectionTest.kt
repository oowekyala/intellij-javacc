package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.ide.inspections.LoopInRegexInspection.Companion.makeMessageImpl

/**
 * @author Clément Fournier
 * @since 1.1
 */
class LoopInRegexInspectionTest : JccInspectionTestBase(LoopInRegexInspection()) {


    private fun String.warning(vararg path: String) =
        errorAnnot(trimIndent(), makeMessageImpl(path.toList()))


    fun testSelfRecursion() = checkByText(
        """
            $DummyHeader

            TOKEN: {
                ${"<FOO: <FOO>>".warning("FOO", "FOO")}
            }
        """
    )

    fun testMutualRecursion() = checkByText(
        """
            $DummyHeader
            TOKEN:{
                  ${"<FOO:<BAR>>".warning("FOO", "BAR", "FOO")}
                | <BAR : <FOO>>
            }
        """
    )

    fun testIndirectRecursion() = checkByText(
        """
            $DummyHeader
            TOKEN:{
                  ${"<FOO:<BAR>>".warning("FOO", "BAR", "BAZ", "FOO")}
                | <BAR: <BAZ>>
                | <BAZ: <FOO>>
            }

        """
    )

    fun testLoopAtAnyPos() = checkByText(
        """
            $DummyHeader
            TOKEN:{
                  ${"<FOO: (\"f\")? <BAR>>".warning("FOO", "BAR", "BAZ", "FOO")}
                | <BAR: <BAZ>>
                | <BAZ: <FOO>>
            }

        """
    )


}