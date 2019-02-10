package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.ide.inspections.LeftRecursiveProductionInspection.Companion.cyclePartMessage
import com.github.oowekyala.ijcc.ide.inspections.LeftRecursiveProductionInspection.Companion.makeMessageImpl

/**
 * @author Clément Fournier
 * @since 1.1
 */
class LeftRecursiveProductionInspectionTest : JccInspectionTestBase(LeftRecursiveProductionInspection()) {


    private fun String.warning(vararg path: String) =
        errorAnnot(trimIndent(), makeMessageImpl(path.toList()))


    private fun String.cyclePart() = errorAnnot(trimIndent(), cyclePartMessage())


    fun testSelfRecursion() = checkByText(
        """

            void Foo(): {} {
                "foo" Foo() // right recursion
            }

             void ${"Bar".warning("Bar", "Bar")}(): {} {
                    ${"Bar()".cyclePart()} "foo" // left recursion
             }

        """.trimIndent().inGrammarCtx()
    )

    fun testMutualLeftRecursion() = checkByText(
        """

            void ${"Foo".warning("Foo", "Bar", "Foo")}(): {} {
                ${"Bar()".cyclePart()} "bar"
            }

            // undetected, because we consider it visited
            void Bar(): {} {
                 ${"Foo()".cyclePart()} "foo" // left recursion
            }

        """.trimIndent().inGrammarCtx()
    )

    fun testIndirectLeftRecursion() = checkByText(
        """

            void ${"Foo".warning("Foo", "Bar", "Baz", "Foo")}(): {} {
                ${"Bar()".cyclePart()} "bar"
            }

            // undetected
            void Bar(): {} {
                ("foo")? ${"Baz()".cyclePart()} "foo"
            }

            // undetected
            void Baz(): {} {
                ${"Foo()".cyclePart()} "foo"
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


    fun testNestedLeftRecursionPos() = checkByText(
        """

            void Foo(): {} {
                Foo2() "bar"
            }

            void ${"Foo2".warning("Foo2", "Bar", "Baz", "Foo2")}(): {} {
                ${"Bar()".cyclePart()} "bar"
            }

            void Bar(): {} {
                ("foo")* ${"Baz()".cyclePart()} "foo"
            }


            void Baz(): {} {
               ${"Foo2()".cyclePart()} "foo"
            }
        """.trimIndent().inGrammarCtx()
    )

}