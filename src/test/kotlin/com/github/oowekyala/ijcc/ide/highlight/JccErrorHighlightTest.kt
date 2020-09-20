package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.util.JccAnnotationTestBase

/**
 * Miscellaneous highlight tests.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccErrorHighlightTest : JccAnnotationTestBase() {


    fun `test private regex definition in BNF`() = checkByText(
        """
            TOKEN: {
             <#FOO: "foo">
            }

            void Foo():{} {
              <<error descr="Private (with a #) regular expression cannot be defined within grammar productions">#BAR</error>: "flex">
            }
        """.inGrammarCtx()
    )

    fun `test private regex reference from BNF`() = checkByText(
        """
            TOKEN: {
             <#FOO: "foo">
            }

            void Foo():{} {
              <<error descr="Token name \"FOO\" refers to a private (with a #) regular expression">FOO</error>>
            }
        """.inGrammarCtx()
    )

    fun `test private regex literal`() = checkByText(
        """
            TOKEN: {
             <#FOO: "foo">
            }

            void Foo():{} {
              <error descr="String token \"foo\" has been defined as a private (#) regular expression">"foo"</error>
            }
        """.inGrammarCtx()
    )


    fun `test private regex reference NEG`() = checkByText(
        """
            TOKEN: {
               <#FOO: "foo">
             | <BAR: <FOO> "tail" >
             | <OREO: "foo" "f">
            }
        """.inGrammarCtx()
    )


    fun `test private special-token regex reference NEG`() = checkByText(
        """
            SPECIAL_TOKEN: {
                < #PRIV: "oha" >
            }

            TOKEN: {
               <FOO: <PRIV>>
             | <BAR: <PRIV> | "h" >
            }
        """.inGrammarCtx()
    )


    fun `test duplicate lexical name NEG`() = checkByText(
        """
            <FLIP, FLOP> TOKEN: {
                "flopp"
            }
        """.inGrammarCtx()
    )


    fun `test duplicate lexical name POS`() = checkByText(
        """
            <<error descr="Duplicate lexical state name FLIP">FLIP</error>, FLOP, <error descr="Duplicate lexical state name FLIP">FLIP</error>> TOKEN: {
                "flopp"
            }
        """.inGrammarCtx()
    )

    fun `test empty character list POS`() = checkByText(
        """
            TOKEN: {
                < <error descr="Empty character set is not allowed as it will not match any character">[]</error> >
            }
        """.inGrammarCtx()
    )

    fun `test empty character list NEG`() = checkByText(
        """
             TOKEN: {
                < ~[] >
            }
        """.inGrammarCtx()
    )


    fun `test usage of SKIP kind`() = checkByText(
        """
        $DummyHeader

        SKIP : {
          <Foo: "foo">
        }

        void Foo() :{}{
          <boo:${"\"foo\"".stringTokenHasWrongKind(actualKind = RegexKind.SKIP)}>
        }
        """
    )

    fun `test undefined token backward reference token to token NEG`() = checkByText(
        """
           $DummyHeader
            TOKEN: {
               <boo:"str">
             | <foo:<boo>>
            }

        """
    )

    fun `test undefined token forward reference token to token NEG`() = checkByText(
        """
           $DummyHeader
            TOKEN: {
               <foo:<boo>>
             | <boo:"str">
            }

        """
    )


    fun `test undefined token backward reference token to bnf NEG`() = checkByText(
        """
           $DummyHeader


            void Foo():
            {}
            {
                <boo : "foo">
            }

            TOKEN: {
               <foo: <boo>>
            }
        """
    )


    fun `test undefined token forward reference token to bnf NEG`() = checkByText(
        """
           $DummyHeader
            TOKEN: {
               <foo:<boo>>
            }

            void Foo():
            {}
            {
                <boo : "foo">
            }

        """
    )

    fun `test undefined token backward reference bnf to bnf NEG`() = checkByText(
        """
           $DummyHeader


            void Foo():
            {}
            {
                <boo : "foo">
                <foo: <boo>>
            }

        """
    )

    fun `test undefined token forward reference bnf to bnf NEG`() = checkByText(
        """
           $DummyHeader


            void Foo():
            {}
            {
                <foo: <boo>>
                <boo : "foo">
            }

        """
    )

    fun `test undefined token backward reference bnf to token NEG`() = checkByText(
        """
           $DummyHeader

            void Foo():
            {}
            {
                <boo : "foo">
            }


            TOKEN: {
               <foo:<boo>>
            }

        """
    )

    fun `test undefined token forward reference bnf to token NEG`() = checkByText(
        """
           $DummyHeader
            TOKEN: {
               <foo:<boo>>
            }

            void Foo():
            {}
            {
                <boo : "foo">
            }

        """
    )


    fun `test token ref can't define itself bnf `() = checkByText(
        """
           $DummyHeader

            void Foo():
            {}
            {
                <${"foo".undefinedStringToken()}>
            }

        """
    )


    fun `test token ref can't define itself bnf in container`() = checkByText(
        """
           $DummyHeader

            void Foo():
            {}
            {
                <<${"foo".undefinedStringToken()}>>
            }

        """
    )

    fun `test FP with private regexp, issue #17`() = checkByText(
        """
           $DummyHeader

            // note: the issue is actually in the inspection BnfStringCanNeverBeMatched

            <DEFAULT> TOKEN : {
                  <#_FRAGMENT:   ["0"-"9"] >
                | <QUOTED:        "\"" ( <_FRAGMENT> )* "\"">
                | <ZERO: "0" >
            }

            public void Rule() : { }
            {
              <QUOTED> | <ZERO>
            }

        """
    )

    private fun String.undefinedStringToken() =
        errorAnnot(
            this,
            JccErrorMessages.undefinedTokenName(name = this)
        )


    private fun String.stringTokenHasWrongKind(literalText: String = this, actualKind: RegexKind) =
        errorAnnot(
            this,
            JccErrorMessages.stringLiteralIsNotToken(regexText = literalText, actualRegexKind = actualKind)
        )


}
