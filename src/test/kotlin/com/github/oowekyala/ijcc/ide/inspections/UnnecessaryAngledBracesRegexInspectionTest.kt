package com.github.oowekyala.ijcc.ide.inspections

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnecessaryAngledBracesRegexInspectionTest : JccInspectionTestBase(UnnecessaryAngledBracesRegexInspection()) {


    private fun warnung(s: String) = warningAnnot(s, UnnecessaryAngledBracesRegexInspection.ProblemDescription)

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


    fun testUnclosedBraces() = checkByText(
        """
            $DummyHeader

            TOKEN: {
              < #FOO: "foo" >
            }

            void Foo():{} {
              < "foo"<EOLError descr="'>' expected, got '}'"></EOLError>
            }

        """.trimIndent()
    )


}