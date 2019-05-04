package com.github.oowekyala.jjtx.samples

import com.github.oowekyala.ijcc.util.indent
import io.kotlintest.should

/**
 * @author Clément Fournier
 */
class TypeHierarchyTests : JjtxTestBase() {

    val baseCtx = contextBuilder {
        jccFile =
            """
                $DummyHeader

                void Foo(): {} {}
                void SomeExpr(): {} {}
                void BarExpr(): {} {}

            """
    }


    fun `test empty config adopts nodes`() {

        val ctx = baseCtx.copy(opts = JsonOpts("{}")).newCtx()

        ctx.jjtxOptsModel.typeHierarchy should matchRoot("Node") {
            node("Foo")
            node("SomeExpr")
            node("BarExpr")
        }
    }

    fun `test different root`() {

        val ctx = baseCtx.withYamlOpts {
            """
        jjtx:
            typeHierarchy:
              "MyRoot"

            """.trimIndent()
        }

        ctx.jjtxOptsModel.typeHierarchy should matchRoot("dummy.grammar.MyRoot") {
            node("dummy.grammar.Foo")
            node("dummy.grammar.SomeExpr")
            node("dummy.grammar.BarExpr")
        }
    }

    fun `test package discrepancy`() {

        val ctx = baseCtx.withYamlOpts {
            """
        jjtx:
            nodePackage: "com.overrides"
            typeHierarchy:
              "MyRoot"

            """.trimIndent()
        }

        ctx.jjtxOptsModel.typeHierarchy should matchRoot("dummy.grammar.MyRoot") {
            node("dummy.grammar.Foo")
            node("dummy.grammar.SomeExpr")
            node("dummy.grammar.BarExpr")
        }
    }


    infix fun CtxBuilder.withYamlOpts(opts: () -> String) = this.copy(
        opts = opts().asYamlOpts()
    ).newCtx()
}

fun String.asYamlTh(): YamlOpts =
    YamlOpts(
        """
        jjtx:
            typeHierarchy:
                ${this.trimIndent().indent(4)}
    """.trimIndent()
    )

fun String.asYamlOpts(): YamlOpts = YamlOpts(this)
