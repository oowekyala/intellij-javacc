package com.github.oowekyala.ijcc.lang

import com.github.oowekyala.ijcc.lang.model.LexicalState
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccParenthesizedExpansionUnitImpl
import com.github.oowekyala.ijcc.lang.util.matchPsi
import io.kotlintest.should
import io.kotlintest.shouldBe
import org.junit.Test

/**
 * @author Clément Fournier
 * @since 1.0
 */
class AstStructureTests : ParserTestDsl() {


    @Test
    fun testAssignmentPos() {
        "a=<REF>" should matchExpansion<JccAssignedExpansionUnit> {
            it.javaAssignmentLhs shouldBe child {
                it.javaName shouldBe child {
                    child<JccIdentifier> {
                        it.name shouldBe "a"
                    }
                }
            }

            it.assignableExpansionUnit shouldBe child<JccRegexExpansionUnit> {
                it.regularExpression shouldBe child<JccRefRegularExpression> {
                    it.unit shouldBe child {
                        it.nameIdentifier shouldBe child {
                            it.name shouldBe "REF"
                        }
                    }
                }
            }
        }

        "a=\"h\"" should matchExpansion<JccAssignedExpansionUnit> {

            it.javaAssignmentLhs shouldBe child {
                it.javaName shouldBe child {
                    child<JccIdentifier> {
                        it.name shouldBe "a"
                    }
                }
            }

            it.assignableExpansionUnit shouldBe child<JccRegexExpansionUnit> {
                it.regularExpression shouldBe child<JccLiteralRegularExpression> {
                    it.unit shouldBe child {}
                }
            }
        }

        "a=< f: \"olol\">" should matchExpansion<JccAssignedExpansionUnit> {

            it.javaAssignmentLhs shouldBe child {
                it.javaName shouldBe child {
                    child<JccIdentifier> {
                        it.name shouldBe "a"
                    }
                }
            }

            it.assignableExpansionUnit shouldBe child<JccRegexExpansionUnit> {

                it.regularExpression shouldBe child<JccNamedRegularExpression> {
                    it.nameIdentifier shouldBe child {
                        it.name shouldBe "f"
                    }

                    it.regexElement shouldBe child<JccLiteralRegexUnit> {}
                }
            }
        }

        "c=foo()" should matchExpansion<JccAssignedExpansionUnit> {

            it.javaAssignmentLhs shouldBe child {
                it.javaName shouldBe child {
                    child<JccIdentifier> {
                        it.name shouldBe "c"
                    }
                }
            }

            it.assignableExpansionUnit shouldBe child<JccNonTerminalExpansionUnit> {

                it.nameIdentifier shouldBe child {
                    it.name shouldBe "foo"
                }
                it.javaExpressionList shouldBe child {}
            }
        }
    }


    @Test
    fun testExpansionPrecedence() {

        "\"foo\"" should matchExpansion<JccRegexExpansionUnit> {
            it.regularExpression shouldBe child<JccLiteralRegularExpression> {
                it.unit shouldBe child {}
            }
        }

        "\"foo\" \"bar\"" should matchExpansion<JccExpansionSequence> {

            child<JccRegexExpansionUnit>(ignoreChildren = true) {}
            child<JccRegexExpansionUnit>(ignoreChildren = true) {}
        }

        "\"ff\" | \"cd\"" should matchExpansion<JccExpansionAlternative> {

            child<JccRegexExpansionUnit>(ignoreChildren = true) { }

            child<JccRegexExpansionUnit>(ignoreChildren = true) { }
        }


        "\"ff\" | \"cd\" #F" should matchExpansion<JccExpansionAlternative> {

            child<JccRegexExpansionUnit>(ignoreChildren = true) { }

            // FIXME should be so, is an ExpansionUnitImpl!! bug in the parser generator
            child<JccScopedExpansionUnit>(ignoreChildren = true) { }
        }

        """"ff" | <baz> | "f" foo()""" should matchExpansion<JccExpansionAlternative> {
            // check that the node is not left recursive

            child<JccRegexExpansionUnit>(ignoreChildren = true) { }

            child<JccRegexExpansionUnit>(ignoreChildren = true) { }

            child<JccExpansionSequence> {
                child<JccRegexExpansionUnit>(ignoreChildren = true) { }
                child<JccNonTerminalExpansionUnit> {
                    child<JccIdentifier> { it.name shouldBe "foo" }
                    child<JccJavaExpressionList> { }

                    it.name shouldBe "foo"
                }
            }
        }
    }


    @Test
    fun testExpansionDeletionInAlternative() {


        val paren = """(<baz> | <bar> | <boo>)""".asExpansion() as JccParenthesizedExpansionUnit

        paren should matchPsi<JccParenthesizedExpansionUnit> {
            it.expansion shouldBe child<JccExpansionAlternative> {
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
            }
        }


        val alternative = paren.expansion as JccExpansionAlternative
        alternative.expansionList[1].delete()

        paren should matchPsi<JccParenthesizedExpansionUnit> {
            it.expansion shouldBe child<JccExpansionAlternative> {
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
            }
        }

        // whitespace errors will be handled by a (maybe) future formatter

        paren.text shouldBe "(<baz>  | <boo>)"

        alternative.expansionList[1].delete()

        paren should matchPsi<JccParenthesizedExpansionUnitImpl> {
            // not an alternative anymore
            it.expansion shouldBe child<JccRegexExpansionUnit>(ignoreChildren = true) {}
        }

        paren.text shouldBe "(<baz>)"

    }


    @Test
    fun testExpansionDeletionInSequence() {


        val paren = """(<baz> <bar> <boo>)""".asExpansion() as JccParenthesizedExpansionUnit

        paren should matchPsi<JccParenthesizedExpansionUnit> {
            it.expansion shouldBe child<JccExpansionSequence> {
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
            }
        }


        val alternative = paren.expansion as JccExpansionSequence
        alternative.expansionUnitList[1].delete()

        paren should matchPsi<JccParenthesizedExpansionUnit> {
            it.expansion shouldBe child<JccExpansionSequence> {
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
            }
        }

        // whitespace errors will be handled by a (maybe) future formatter

        paren.text shouldBe "(<baz>  <boo>)"

        alternative.expansionUnitList[1].delete()

        paren should matchPsi<JccParenthesizedExpansionUnitImpl> {
            // not a sequence anymore
            it.expansion shouldBe child<JccRegexExpansionUnit>(ignoreChildren = true) {}
        }

        paren.text shouldBe "(<baz>)"

    }

    @Test
    fun testExpansionDeletionInSequenceAndAlternative() {


        val paren = """(<baz> | <bar> <boo>)""".asExpansion() as JccParenthesizedExpansionUnit



        paren should matchPsi<JccParenthesizedExpansionUnit> {
            it.expansion shouldBe child<JccExpansionAlternative> {
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
                child<JccExpansionSequence> {
                    child<JccRegexExpansionUnit>(ignoreChildren = true) {}
                    child<JccRegexExpansionUnit>(ignoreChildren = true) {}
                }
            }
        }


        val alternative = paren.expansion as JccExpansionAlternative
        alternative.expansionList[0].delete()


        var sequence: JccExpansionSequence? = null

        paren should matchPsi<JccParenthesizedExpansionUnit> {
            // not an alternative anymore
            sequence = child {
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexExpansionUnit>(ignoreChildren = true) {}
            }
        }

        // whitespace errors will be handled by a (maybe) future formatter

        paren.text shouldBe "(<bar> <boo>)"

        sequence!!.expansionUnitList[0].delete()

        paren should matchPsi<JccParenthesizedExpansionUnitImpl> {
            // not a sequence anymore
            it.expansion shouldBe child<JccRegexExpansionUnit>(ignoreChildren = true) {}
        }

        paren.text shouldBe "(<boo>)"

    }


    @Test
    fun testIgnoreCaseRegex() {

        val prod = """ TOKEN [IGNORE_CASE] : {
             "foo"
            }
        """.asProduction()


        prod should matchPsi<JccRegexProduction> {
            it.isIgnoreCase shouldBe true
            it.lexicalStateList shouldBe null
            it.lexicalStatesNameOrEmptyForAll shouldBe LexicalState.JustDefaultState
            it.regexKind shouldBe child {
                it.text shouldBe "TOKEN"
            }

            child<JccRegexSpec> {
                it.lexicalActions shouldBe null
                it.lexicalStateTransition shouldBe null
                it.name shouldBe null
                it.nameIdentifier shouldBe null

                it.isIgnoreCase shouldBe true
                it.isPrivate shouldBe false
                it.lexicalStatesOrEmptyForAll shouldBe LexicalState.JustDefaultState

                it.regularExpression shouldBe child<JccLiteralRegularExpression> {

                    it.unit shouldBe child {}
                }
            }
        }


    }

}
