package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.lang.util.ParseUtilsMixin
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


    fun String.usageWarn(supersedingName: String?) =
            errorAnnot(this, JccErrorMessages.stringLiteralMatchedbyIgnoreCaseCannotBeUsedInBnf(supersedingName))

}