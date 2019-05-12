package com.github.oowekyala.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.util.indent
import com.github.oowekyala.jjtx.JjtxContextTestBase
import com.github.oowekyala.jjtx.JsonOpts
import com.github.oowekyala.jjtx.YamlOpts
import com.github.oowekyala.jjtx.testutil.matchRoot
import com.github.oowekyala.jjtx.testutil.node
import com.github.oowekyala.jjtx.util.*
import io.kotlintest.should

/**
 * @author Clément Fournier
 */
class TypeHierarchyTests : JjtxContextTestBase() {

    private val baseCtx = testBuilder {
        jccFile =
            """

                PARSER_BEGIN(dummy)

                public class dummy {}

                PARSER_END(dummy)

                void Foo(): {} {}
                void SomeExpr(): {} {}
                void BarExpr(): {} {}

            """
    }


    fun `test empty config adopts nodes`() {
        baseCtx
            .copy(opts = JsonOpts("{\"jjtx.nodePrefix\": \"\"}"))
            .doTest {
                myCtx.jjtxOptsModel.typeHierarchy should matchRoot("Node") {
                    node("SomeExpr")
                    node("Foo")
                    node("BarExpr")
                }
        }
    }

    fun `test different root`() {

        baseCtx.withYamlOpts {
            """
        jjtx:
            nodePackage: "dummy.grammar"
            nodePrefix: ""
            typeHierarchy:
              "MyRoot"

            """.trimIndent()
        }.doTest {
            myCtx.jjtxOptsModel.typeHierarchy should matchRoot("dummy.grammar.MyRoot") {
                node("dummy.grammar.SomeExpr")
                node("dummy.grammar.Foo")
                node("dummy.grammar.BarExpr")
            }
        }


    }

    fun `test package discrepancy`() {
        baseCtx.copy(

            jccFile = "options { NODE_PACKAGE =\"dummy.grammar\" } " + baseCtx.jccFile,
            opts =
            """
        jjtx:
            nodePackage: "com.overrides"
            nodePrefix: ""
            typeHierarchy:
              "MyRoot"

            """.trimIndent().asYamlOpts()
        )
            .doTest {
                myCtx.jjtxOptsModel.typeHierarchy should matchRoot("com.overrides.MyRoot") {
                    node("com.overrides.SomeExpr")
                    node("com.overrides.Foo")
                    node("com.overrides.BarExpr")
                }
            }
    }

    fun `test prefix override`() {
        baseCtx.copy(
            opts =
            """
        jjtx:
            nodePackage: "com.overrides"
            nodePrefix: "P"
            typeHierarchy:
              "%MyRoot"

            """.trimIndent().asYamlOpts()
        )
            .doTest {
                myCtx.jjtxOptsModel.typeHierarchy should matchRoot("com.overrides.MyRoot") {
                    node("com.overrides.PSomeExpr")
                    node("com.overrides.PFoo")
                    node("com.overrides.PBarExpr")
                }
            }
    }


    infix fun CtxTestBuilder.withYamlOpts(opts: () -> String) = this.copy(
        opts = opts().asYamlOpts()
    )
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
