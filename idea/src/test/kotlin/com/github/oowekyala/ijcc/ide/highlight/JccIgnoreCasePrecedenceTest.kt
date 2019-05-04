package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.util.JccAnnotationTestBase

/**
 * @author Clément Fournier
 * @since 1.1
 */
class JccIgnoreCasePrecedenceTest : JccAnnotationTestBase() {

    fun `test ignore case precedence POS`() = checkByText(
        """
            $DummyHeader

            TOKEN [IGNORE_CASE] : {
                < FOO: "foo" >
            }

            void Foo():{} {
                <boo:${"\"foo\"".usageWarn("FOO")}>
            }

        """
    )

    fun `test ignore case precedence NEG`() = checkByText(
        """
            TOKEN : {
                < FOO: "foo" >
            }

            void Foo():{} {
              "foo"
            }

        """.inGrammarCtx()
    )


    fun `test duplicate with different label and IGNORE_CASE POS`() = checkByText(
        """
        $DummyHeader

        TOKEN [IGNORE_CASE]: {
          <Foo: "abc">
        }

        void Foo() :{}{
          <boo:${"\"abc\"".usageWarn("Foo")}>
        }

        """
    )


    fun `test partial IGNORE_CASE supercedence POS`() = checkByText(
        """
            $DummyHeader

            void Foo(): {} {
            "foo"
            }

            TOKEN [IGNORE_CASE]: {

                 ${"<FOO: \"foo\">".partialWarn(supersedingLine = 14)}

            }
        """
    )

    fun `test partial IGNORE_CASE supercedence with spec POS`() = checkByText(
        """
            $DummyHeader

            TOKEN: {
              "foo"

            }


            TOKEN [IGNORE_CASE]: {

                 ${"<FOO: \"foo\">".partialWarn(supersedingLine = 14)}

            }

        """
    )


    fun `test partial IGNORE_CASE supercedence with rename POS`() = checkByText(
        """
            $DummyHeader

            TOKEN: {
              <FLAB: "foo">
            }


            TOKEN [IGNORE_CASE]: {

               ${"<FOO: \"foo\">".partialWarn(supersedingName = "FLAB", supersedingLine = 14)}

            }

        """
    )

    fun `test partial IGNORE_CASE supercedence in BNF with container POS`() = checkByText(
        """
            $DummyHeader

            void Fff(): {} {
              < "foo">

            }


            TOKEN [IGNORE_CASE]: {

               ${"<FOO: \"foo\">".partialWarn(supersedingLine = 14)}

            }

        """
    )

    fun `test partial IGNORE_CASE supercedence in BNF with ref NEG`() = checkByText(
        """
            $DummyHeader

            void Fff():
            {}
            {
              <FOO>

            }


            TOKEN [IGNORE_CASE]: {

               <FOO: "foo">

            }

        """
    )


    private fun String.usageWarn(supersedingName: String?) =
        errorAnnot(this, JccErrorMessages.stringLiteralMatchedbyIgnoreCaseCannotBeUsedInBnf(supersedingName))


    private fun String.partialWarn(supersedingName: String? = null,
                                   supersedingText: String = "\"foo\"",
                                   supersedingLine: Int) =
        warningAnnot(
            this,
            JccErrorMessages.stringLiteralWithIgnoreCaseIsPartiallySupercededImpl(
                supersedingName,
                supersedingText,
                supersedingLine
            )
        )

}