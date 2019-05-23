package com.github.oowekyala.ijcc.ide.inspections

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnecessaryParenthesesInspectionTest : JccInspectionTestBase(JccUnnecessaryParenthesesInspection()) {


    private fun warnung(s: String) = warningAnnot(s, JccUnnecessaryParenthesesInspection.ProblemDescription)


    fun testReferenceString() = checkByText(
        """
            $DummyHeader

            TOKEN: {
              < FOO: "foo" >
            }

            void Foo() :{}{
              ${warnung("( <FOO> )")}
            }

        """.trimIndent()
    )

    fun testParen() = checkByText(
        """< ("foo") > """.inExpansionCtx()
    )

    fun testScoped() = checkByText(
        """
            $DummyHeader

            TOKEN: {
              < #FOO: "foo" >
            }

            void Foo():{} {
              ( "f" #Foo ) #Bar
            }

        """.trimIndent()
    )




}
