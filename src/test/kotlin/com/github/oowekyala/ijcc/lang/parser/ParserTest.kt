package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.util.AssertionMatcher
import com.github.oowekyala.ijcc.lang.util.PsiSpec
import com.github.oowekyala.ijcc.lang.util.matchPsi
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import io.kotlintest.should
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class ParserTest : LightCodeInsightFixtureTestCase() {


    private inline fun <reified N : PsiElement> matchExpansion(ignoreChildren: Boolean = false,
                                                               noinline nodeSpec: PsiSpec<N>): AssertionMatcher<String> =
            {
                JccElementFactory.createBnfExpansion(project, it) should matchPsi(ignoreChildren, nodeSpec)
            }


    fun testExpansionPrecedence() {

        "\"foo\"" should matchExpansion<JccRegexpExpansionUnit> {
            it.regularExpression shouldBe child<JccLiteralRegularExpression> {
                child<JccLiteralRegexpUnit>{}
            }
        }

        "\"foo\" \"bar\"" should matchExpansion<JccExpansionSequence> {

            child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
            child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
        }

        "\"ff\" | \"cd\"" should matchExpansion<JccExpansionAlternative> {

            child<JccRegexpExpansionUnit>(ignoreChildren = true) { }

            child<JccRegexpExpansionUnit>(ignoreChildren = true) { }
        }

        """"ff" | <baz> | "f" foo()""" should matchExpansion<JccExpansionAlternative> {
            // check that the node is not left recursive

            child<JccRegexpExpansionUnit>(ignoreChildren = true) { }

            child<JccRegexpExpansionUnit>(ignoreChildren = true) { }

            child<JccExpansionSequence> {
                child<JccRegexpExpansionUnit>(ignoreChildren = true) { }
                child<JccNonTerminalExpansionUnit> {
                    child<JccIdentifier> { it.name shouldBe "foo" }
                    child<JccJavaExpressionList> { }

                    it.name shouldBe "foo"
                }
            }
        }
    }
}