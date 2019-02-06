package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccParenthesizedExpansionUnitImpl
import com.github.oowekyala.ijcc.lang.util.matchPsi
import io.kotlintest.should
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class ParserTest : ParserTestDsl() {


    fun testAssignmentPos() {
        "a=<REF>" should matchExpansion<JccAssignedExpansionUnit> {
            it.javaAssignmentLhs shouldBe child {
                it.javaName shouldBe child {
                    child<JccIdentifier> {
                        it.name shouldBe "a"
                    }
                }
            }

            it.assignableExpansionUnit shouldBe child<JccRegexpExpansionUnit> {
                it.regularExpression shouldBe child<JccRegularExpressionReference> {
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

            it.assignableExpansionUnit shouldBe child<JccRegexpExpansionUnit> {
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

            it.assignableExpansionUnit shouldBe child<JccRegexpExpansionUnit> {

                it.regularExpression shouldBe child<JccNamedRegularExpression> {
                    it.nameIdentifier shouldBe child {
                        it.name shouldBe "f"
                    }

                    it.regexpElement shouldBe child<JccLiteralRegexpUnit> {}
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


    fun testExpansionPrecedence() {

        "\"foo\"" should matchExpansion<JccRegexpExpansionUnit> {
            it.regularExpression shouldBe child<JccLiteralRegularExpression> {
                it.unit shouldBe child {}
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


    fun testExpansionDeletionInAlternative() {


        val paren = """(<baz> | <bar> | <boo>)""".asExpansion() as JccParenthesizedExpansionUnit

        paren should matchPsi<JccParenthesizedExpansionUnit> {
            it.expansion shouldBe child<JccExpansionAlternative> {
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
            }
        }


        val alternative = paren.expansion as JccExpansionAlternative
        alternative.expansionList[1].delete()

        paren should matchPsi<JccParenthesizedExpansionUnit> {
            it.expansion shouldBe child<JccExpansionAlternative> {
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
            }
        }

        // whitespace errors will be handled by a (maybe) future formatter

        paren.text shouldBe "(<baz>  | <boo>)"

        alternative.expansionList[1].delete()

        paren should matchPsi<JccParenthesizedExpansionUnitImpl> {
            // not an alternative anymore
            it.expansion shouldBe child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
        }

        paren.text shouldBe "(<baz>)"

    }


    fun testExpansionDeletionInSequence() {


        val paren = """(<baz> <bar> <boo>)""".asExpansion() as JccParenthesizedExpansionUnit

        paren should matchPsi<JccParenthesizedExpansionUnit> {
            it.expansion shouldBe child<JccExpansionSequence> {
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
            }
        }


        val alternative = paren.expansion as JccExpansionSequence
        alternative.expansionUnitList[1].delete()

        paren should matchPsi<JccParenthesizedExpansionUnit> {
            it.expansion shouldBe child<JccExpansionSequence> {
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
            }
        }

        // whitespace errors will be handled by a (maybe) future formatter

        paren.text shouldBe "(<baz>  <boo>)"

        alternative.expansionUnitList[1].delete()

        paren should matchPsi<JccParenthesizedExpansionUnitImpl> {
            // not a sequence anymore
            it.expansion shouldBe child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
        }

        paren.text shouldBe "(<baz>)"

    }

    fun testExpansionDeletionInSequenceAndAlternative() {


        val paren = """(<baz> | <bar> <boo>)""".asExpansion() as JccParenthesizedExpansionUnit



        paren should matchPsi<JccParenthesizedExpansionUnit> {
            it.expansion shouldBe child<JccExpansionAlternative> {
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
                child<JccExpansionSequence> {
                    child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
                    child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
                }
            }
        }


        val alternative = paren.expansion as JccExpansionAlternative
        alternative.expansionList[0].delete()


        var sequence: JccExpansionSequence? = null

        paren should matchPsi<JccParenthesizedExpansionUnit> {
            // not an alternative anymore
            sequence = child {
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
                child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
            }
        }

        // whitespace errors will be handled by a (maybe) future formatter

        paren.text shouldBe "(<bar> <boo>)"

        sequence!!.expansionUnitList[0].delete()

        paren should matchPsi<JccParenthesizedExpansionUnitImpl> {
            // not a sequence anymore
            it.expansion shouldBe child<JccRegexpExpansionUnit>(ignoreChildren = true) {}
        }

        paren.text shouldBe "(<boo>)"

    }

}