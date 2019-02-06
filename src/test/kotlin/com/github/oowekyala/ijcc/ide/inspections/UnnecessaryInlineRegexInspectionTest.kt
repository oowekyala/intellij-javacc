package com.github.oowekyala.ijcc.ide.inspections

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnecessaryInlineRegexInspectionTest : JccInspectionTestBase(UnnecessaryInlineRegexInspection()) {


    private fun warnung(s: String) = warningAnnot(s, UnnecessaryInlineRegexInspection.ProblemDescription)

    fun testLiteralString() = checkByText(
        warnung("""< "foo" >""").inExpansionCtx()
    )

    fun testReferenceString() = checkByText(
        """
            $DummyHeader

            TOKEN: {
              < FOO: "foo" >
            }

            void Foo() :{}{
              ${warnung("< <FOO> >")}
            }

        """.trimIndent()
    )

    fun testParen() = checkByText(
        """< ("foo") > """.inExpansionCtx()
    )

    fun testPrivateReference() = checkByText(
        """
            $DummyHeader

            TOKEN: {
              < #FOO: "foo" >
            }

            void Foo():{} {
              < <FOO> >
            }

        """.trimIndent()
    )


}