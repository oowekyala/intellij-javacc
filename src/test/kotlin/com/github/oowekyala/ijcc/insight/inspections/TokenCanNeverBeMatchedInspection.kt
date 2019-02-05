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


}