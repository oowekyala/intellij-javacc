package com.github.oowekyala.ijcc.ide.inspections

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnamedRegexInspectionTest : JccInspectionTestBase(UnnamedRegexInspection()) {


    private fun generic(s: String) = warningAnnot(s, UnnamedRegexInspection.GenericProblemDesc)
    private fun reference(s: String) = warningAnnot(s, UnnamedRegexInspection.FreeStandingReferenceProblemDesc)

    fun testLiteralString() = checkByText(
        """< "foo" >""".inExpansionCtx()
    )

    fun testAll() = checkByText(
        """
            $DummyHeader

            TOKEN: {
                < FOO: "foo" >
              | ${generic("< \" Foo \" >")}
              | ${reference("< FOO >")}
            }

            void Foo() :{}{
              < <FOO> > < "foobar" >
            }

        """.trimIndent()
    )

    fun testParen() = checkByText(
        generic("""< ("foo") >""").inExpansionCtx()
    )


}