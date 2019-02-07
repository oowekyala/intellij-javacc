package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.util.JccAnnotationTestBase

/**
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

    fun `test ignore case precedence POS`() = checkByText(
        """
            TOKEN [IGNORE_CASE] : {
                < FOO: "foo" >
            }

            void Foo():{} {
              <error descr="String is matched by an IGNORE_CASE regular expression and should refer to the token by name (<FOO>)">"foo"</error>
            }

        """.inGrammarCtx()
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

    fun `test ignore case duplicates`() = checkByText(
        """
            TOKEN [IGNORE_CASE]: {
                  <Foo: "FOO" >
                | <error descr="Duplicate definition of string token \"foo\" (see <Foo>, which is case-insensitive)"><FO: "foo" ></error>
            }
        """.inGrammarCtx()
    )

    fun `test duplicate string synthetic precedence`() = checkByText(
        """
            void Foo() :{}{
              "Foo"
            }

            <*> TOKEN : {
                 <error descr="Duplicate definition of string token \"Foo\" (implicitly defined at line 12)">< Foo: "Foo" ></error>
            }
        """.inGrammarCtx()
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
            void Foo() :{}{
              "Foo"
            }

            <DEFAULT> TOKEN : {
                 <error descr="Duplicate definition of string token \"Foo\" (implicitly defined at line 12)">< Foo: "Foo" ></error>
            }
        """.inGrammarCtx()
    )


    fun `test duplicate with multiple state refs`() = checkByText(
        """

            <*> TOKEN: {
               "abc"
            }

            <A> TOKEN: {
              <error descr="Duplicate definition of string token \"abc\" (unnamed) in state A"><Foo: "abc"></error>
            }

            void Foo() :{}{
              "abc" // DEFAULT
            }

            TOKEN: {
              <error descr="Duplicate definition of string token \"abc\" (unnamed)">"abc"</error>
            }

        """.inGrammarCtx()
    )


    fun `test duplicate with multiple state refs and inline regex`() = checkByText(
        """

            <A> TOKEN: {
              <Foo: "abc">
            }

            void Foo() :{}{
              <"abc">
            }

            TOKEN: {
              <error descr="Duplicate definition of string token \"abc\" (implicitly defined at line 17)">"abc"</error>
            }

        """.inGrammarCtx()
    )

    fun `test duplicate with different label NEG`() = checkByText(
        """

            TOKEN: {
              <Foo: "abc">
            }

            void Foo() :{}{
              <boo:"abc">
            }

        """.inGrammarCtx()
    )

    fun `test duplicate with different label POS`() = checkByText(
        """
            void Foo() :{}{
              <boo:"abc">
            }

            TOKEN: {
              <error descr="Duplicate definition of string token \"abc\" (implicitly defined at line 12)"><Foo: "abc"></error>
            }


        """.inGrammarCtx()
    )


}