package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.lang.model.LexicalState
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.util.ParseUtilsMixin
import com.github.oowekyala.ijcc.util.JccAnnotationTestBase

/**
 * @author Clément Fournier
 * @since 1.1
 */
class JccDuplicateStringTokenTest : JccAnnotationTestBase() {


    fun `test duplicate string synthetic precedence`() = checkByText(
        """
            $DummyHeader
            void Foo() :{}{
              "foo"
            }

            <*> TOKEN : {
                ${"< Foo: \"foo\" >".warn(tokenLine = 13)}
            }
        """
    )

    fun `test duplicate in other state`() = checkByText(
        """
            void Foo() :{}{
              "Foo"
            }

            <A> TOKEN : {
                 < Foo: "Foo" >
            }
        """.inGrammarCtx()
    )

    fun `test duplicate string synthetic precedence in explicit default POS`() = checkByText(
        """
            $DummyHeader

            void Foo() :{}{
              "foo"
            }

            <DEFAULT> TOKEN : {
                   ${"<Foo: \"foo\" >".warn(tokenLine = 14)}
            }
        """
    )


    fun `test duplicate with multiple state refs`() = checkByText(
        """

            <*> TOKEN: {
               "foo"
            }

            <A> TOKEN: {
             ${"<Foo: \"foo\" >".warn( stateName = "A", tokenName = null)}
            }

            void Foo() :{}{
              "foo" // DEFAULT
            }

            TOKEN: {
              ${"\"foo\"".warn(tokenName = null)}
            }

        """.inGrammarCtx()
    )


    fun `test duplicate with multiple state refs and inline regex`() = checkByText(
        """
            $DummyHeader

            <A> TOKEN: {
              <FOO: "foo">
            }

            void Foo() :{}{
              <"foo">
            }

            TOKEN: {
              ${"\"foo\"".warn(tokenLine = 18)}
            }
        """
    )

    fun `test duplicate with different label NEG`() = checkByText(
        """

            TOKEN: {
              <Foo: "foo">
            }

            void Foo() :{}{
              <boo:"foo">
            }

        """.inGrammarCtx()
    )

    fun `test duplicate with different label POS`() = checkByText(
        """
            $DummyHeader

            void Foo() :{}{
              <FOO:"foo">
            }

            TOKEN: {
               ${"<abc: \"foo\" >".warn(tokenLine = 14)}
            }
        """
    )

    fun `test duplicate string token definition NEG`() = checkByText(
        """
            TOKEN: {
               <#FOO: "foo">
             | <BAR: <FOO> >
            }
        """.inGrammarCtx()
    )


    fun `test duplicate string token private definition POS`() = checkByText(
        """
            TOKEN: {
               <#FOO: "foo">
             | <error descr="Duplicate definition of string token \"foo\" (see <FOO>)"><BAR: "foo" ></error>
            }
        """.inGrammarCtx()
    )

    fun `test duplicate string token definition POS`() = checkByText(
        """
            TOKEN: {
               <FOO: "foo">
             | <error descr="Duplicate definition of string token \"foo\" (see <FOO>)"><BAR: "foo" ></error>
            }
        """.inGrammarCtx()
    )


    fun `test ignore case duplicates`() = checkByText(
        """
        ${ParseUtilsMixin.DummyHeader}

        TOKEN [IGNORE_CASE]: {
              <FOO: "FOO" >
            | ${"<FO: \"foo\" >".warn(tokenIsIgnoreCase = true)}
        }
        """
    )


    fun `test duplicate of SKIP kind`() = checkByText(
        """
        $DummyHeader

        void Foo() :{}{
          <boo:"foo">
        }


        SKIP : {
          ${"<Foo: \"foo\">".warn(tokenLine = 14)}
        }
        """
    )

    fun `test nearly label redefinition NEG`() = checkByText(
        """
            $DummyHeader
            TOKEN: {
               <boo:"str">
             | <foo:<boo>>
            }
        """
    )

    fun `test nearly label redefinition NEG 2`() = checkByText(
        """
            $DummyHeader
            TOKEN: {
               <boo:"str">
             | <foo:<boo>>
            }
        """
    )


    private fun String.warn(regexText: String = "\"foo\"",
                            stateName: String = LexicalState.DefaultStateName,
                            tokenLine: Int? = null,
                            tokenName: String? = "FOO",
                            tokenIsIgnoreCase: Boolean = false): String =
            errorAnnot(
                this,
                JccErrorMessages.duplicateStringTokenImpl(
                    regexText = regexText,
                    stateName = stateName,
                    tokenIsExplicit = tokenLine == null,
                    tokenLine = tokenLine,
                    tokenName = tokenName,
                    tokenIsIgnoreCase = tokenIsIgnoreCase
                )
            )

}