package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.insight.inspections.JccUnusedProductionInspection.Companion.ErrorType.UNREACHABLE
import com.github.oowekyala.ijcc.insight.inspections.JccUnusedProductionInspection.Companion.ErrorType.UNUSED

/**
 * @author Clément Fournier
 * @since 1.1
 */
class JccUnusedProductionInspectionTest : JccInspectionTestBase(JccUnusedProductionInspection()) {


    private fun unused(name: String) = warningAnnot(name, UNUSED.makeMessage(name))
    private fun unreachable(name: String) = warningAnnot(name, UNREACHABLE.makeMessage(name))


    fun `test unused`() = checkByText(
        """
        $DummyHeader

        void Foo():
        {}
        {
            "foo"
        }

        void ${unused("Unused")}(): {}{
            "hello"
        }

    """.trimIndent()
    )


    fun `test unreachable`() = checkByText(
        """
        $DummyHeader

        TOKEN: {
        "foo"
        }

        void Foo():
        {}
        {
            "foo"
        }

        void ${unused("Unused")}(): {}{
            "hello" Unreachable()
        }

        void ${unreachable("Unreachable")}(): {}{
            "bazouli"
        }

    """.trimIndent()
    )


    fun `test suppression transitivity`() = checkByText(
        """
        $DummyHeader

        TOKEN: {
        "foo"
        }

        void Foo():
        {}
        {
            "foo"
        }

        //noinspection JavaCCUnusedProduction
        void Unused(): {}{
            "hello" Unreachable()
        }

        void Unreachable(): {}{
            "bazouli"
        }

    """.trimIndent()
    )


}