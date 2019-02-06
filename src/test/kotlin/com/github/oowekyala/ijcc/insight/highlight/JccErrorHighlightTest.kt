package com.github.oowekyala.ijcc.insight.highlight

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


    fun `test duplicate string token definition POS`() = checkByText(
        """
            TOKEN: {
               <error descr="Duplicate definition of string token \"foo\""><#FOO: "foo"></error>
             | <error descr="Duplicate definition of string token \"foo\""><BAR: "foo" ></error>
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


}