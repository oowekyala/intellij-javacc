package com.github.oowekyala.ijcc.ide.inspections

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccInspectionSuppressorTest : JccInspectionTestBase(LookaheadIsNotAtChoicePointInspection()) {


    fun testSuppressSingleComment() = checkByText(
        """
            $DummyHeader

            //noinspection JavaCCLookaheadIsNotAtChoicePoint
            void Foo(): {}
            {
             LOOKAHEAD(1) "foo" "bar"
            }

        """.trimIndent()
    )


    fun testSuppressCStyleComment() = checkByText(
        """
            $DummyHeader

            /* noinspection JavaCCLookaheadIsNotAtChoicePoint */
            void Foo(): {}
            {
             LOOKAHEAD(1) "foo" "bar"
            }

        """.trimIndent()
    )


    fun testSuppressAllEol() = checkByText(
        """
            $DummyHeader

            //noinspection ALL
            void Foo(): {}
            {
             LOOKAHEAD(1) "foo" "bar"
            }

        """.trimIndent()
    )

    fun testSuppressCStyleInlineComment() = checkByText(
        """
            $DummyHeader


            void Foo(): {}
            {
            /* noinspection JavaCCLookaheadIsNotAtChoicePoint */ LOOKAHEAD(1) "foo" "bar"
            }

        """.trimIndent()
    )
}
