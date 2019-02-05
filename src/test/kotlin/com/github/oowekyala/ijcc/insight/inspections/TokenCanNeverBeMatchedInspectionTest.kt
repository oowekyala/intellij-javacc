package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.insight.inspections.TokenCanNeverBeMatchedInspection.Companion.problemDescription

/**
 * @author Clément Fournier
 * @since 1.0
 */
class TokenCanNeverBeMatchedInspectionTest : JccInspectionTestBase(TokenCanNeverBeMatchedInspection()) {


    private fun warning(content: String, realMatchName: String?) =
            warningAnnot(content, problemDescription(realMatchName))

    fun testInAlternative() = checkByText(
        """

           TOKEN: {
               <FOO: "foo">
             | <BAR: "bar" | ${warning("\"foo\"", "FOO")}>
           }
        """.trimIndent().inGrammarCtx()
    )

    fun testInAlternative2() = checkByText(
        """

           TOKEN: {
               <BAR: "bar" | "foo">
             | ${warning("<FOO: \"foo\">", "BAR")}
           }
        """.trimIndent().inGrammarCtx()
    )


    fun testCrossAlternatives() = checkByText(
        """

           TOKEN: {
               <FOO: "foo" | "bar" >
             | <BAR: "qux" | ${warning("\"bar\"", "FOO")} | "quux" >
           }
        """.trimIndent().inGrammarCtx()
    )


    fun testUnnamed() = checkByText(
        """

           TOKEN: {
               < "foo" | "bar" >
             | <BAR: "qux" | ${warning("\"bar\"", null)} | "quux" >
           }
        """.trimIndent().inGrammarCtx()
    )


    fun testWithNonLiteralOverride() = checkByText(
        """

           TOKEN: {
               < "foo" | "bar" > // has higher precedence
             | <NCNAME: (["a"-"z"])+ >
           }
        """.trimIndent().inGrammarCtx()
    )

    fun testWithNonLiteral() = checkByText(
        """

           TOKEN: {
               <NCNAME: (["a"-"z"])+ >
             | < ${warning("\"foo\"", "NCNAME")} | ${warning("\"bar\"", "NCNAME")} >
           }
        """.trimIndent().inGrammarCtx()
    )


    fun testInDifferentProds() = checkByText(
        """

           TOKEN: {
               <NCNAME: (["a"-"z"])+ >
           }

           TOKEN: {
             < ${warning("\"foo\"", "NCNAME")} | ${warning("\"bar\"", "NCNAME")} >
           }
        """.trimIndent().inGrammarCtx()
    )


    fun testInDifferentStates() = checkByText(
        """

           <ASTATE> TOKEN: {
               <NCNAME: (["a"-"z"])+ >
           }

           TOKEN: {
               < "foo" | "bar" > // isn't covered
           }
        """.trimIndent().inGrammarCtx()
    )


    fun testInSameState() = checkByText(
        """

           <ASTATE> TOKEN: {
               <NCNAME: (["a"-"z"])+ >
           }

           <ASTATE> TOKEN: {
               < ${warning("\"foo\"", "NCNAME")} | ${warning("\"bar\"", "NCNAME")} >
           }
        """.trimIndent().inGrammarCtx()
    )


    fun testMultipleStateMatches() = checkByText(
        """
           <ASTATE> TOKEN: {
                  <FOO: "foo" > // has higher precedence
                | < ${warning("\"foo\"", "FOO")} | "bar" >
           }
           // here, NCNAME doesn't match "bar" because it's in a different state. FOO should match "foo" though

           TOKEN: {
               <NCNAME: (["a"-"z"])+ >
           }
        """.trimIndent().inGrammarCtx()
    )

}