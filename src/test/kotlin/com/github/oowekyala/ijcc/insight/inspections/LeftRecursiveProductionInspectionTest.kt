package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.insight.inspections.LeftRecursiveProductionInspection.Companion.makeMessageImpl

/**
 * @author Clément Fournier
 * @since 1.0
 */
class LeftRecursiveProductionInspectionTest : JccInspectionTestBase(LeftRecursiveProductionInspection()) {


    private fun warning(prod: String, path: List<String>) = errorAnnot(prod.trimIndent(), makeMessageImpl(path))


    fun testSelfRecursion() = checkByText(
        """

            void Foo(): {} {
                "foo" Foo() // right recursion
            }

            ${warning("""
            void Bar(): {} {
                Bar() "foo" // left recursion
            }""",
            listOf("Bar", "Bar")
            )}

        """.trimIndent().inGrammarCtx()
    )

    fun testMutualLeftRecursion() = checkByText(
        """

            ${warning(
            """
            void Foo(): {} {
                Bar() "bar"
            }""",
            listOf("Foo", "Bar", "Foo")
            )}

            // undetected, because we consider it visited
            void Bar(): {} {
                Foo() "foo" // left recursion
            }

        """.trimIndent().inGrammarCtx()
    )

    fun testIndirectLeftRecursion() = checkByText(
        """

            ${warning(
            """
            void Foo(): {} {
                Bar() "bar"
            }""",
            listOf("Foo", "Bar", "Baz", "Foo")
            )}

            // undetected
            void Bar(): {} {
                ("foo")? Baz() "foo"
            }

            // undetected
            void Baz(): {} {
                Foo() "foo"
            }
        """.trimIndent().inGrammarCtx()
    )

    fun testIndirectLeftRecursionNeg() = checkByText(
        """

            void Foo(): {} {
                Bar() "bar"
            }

            void Bar(): {} {
                ("foo")+ Baz() "foo"
            }

            void Baz(): {} {
                "baz" #St Foo() "foo"
            }
        """.trimIndent().inGrammarCtx()
    )

}