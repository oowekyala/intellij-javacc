package com.github.oowekyala.ijcc.insight.model

import com.github.oowekyala.ijcc.lang.psi.JccRegexpExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.descendantSequence
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.haveSize
import io.kotlintest.should

/**
 * @author Clément Fournier
 * @since 1.0
 */
class LexicalGrammarTest : JccTestBase() {


    fun testSyntheticTokens() {

        val file = """

            TOKEN: {
              <FOO: "foo">
            }


            void Bar(): {}
            {
              "bar" <FOO>
            }

        """.trimIndent().asJccGrammar()

        val lexicalGrammar = file.lexicalGrammar

        lexicalGrammar.lexicalStates should haveSize(1)

        val default = lexicalGrammar.getLexicalState(LexicalState.DefaultStateName)!!

        val explicitFoo = file.globalTokenSpecs.first().also { check(it.name == "FOO") }
        val implicitBar = file.descendantSequence().first { it.text == "\"bar\"" }.let { it as JccRegexpExpansionUnit }
        default.tokens.shouldContainExactly(
            ExplicitToken(explicitFoo),
            SyntheticToken(implicitBar)
        )

    }


    fun testSyntheticTokensAreDefault() {

        val file = """

            <ASTATE> TOKEN: {
              <FOO: "foo">
            }


            void Bar(): {}
            {
              "bar" <FOO>
            }

        """.trimIndent().asJccGrammar()

        val lexicalGrammar = file.lexicalGrammar

        lexicalGrammar.lexicalStates should haveSize(2)

        val explicitFoo = file.globalTokenSpecs.first().also { check(it.name == "FOO") }
        val implicitBar = file.descendantSequence().first { it.text == "\"bar\"" }.let { it as JccRegexpExpansionUnit }

        lexicalGrammar.getLexicalState(LexicalState.DefaultStateName)!!.tokens.shouldContainExactly(
            SyntheticToken(implicitBar)
        )

        lexicalGrammar.getLexicalState("ASTATE")!!.tokens.shouldContainExactly(
            ExplicitToken(explicitFoo)
        )

    }


    fun testSyntheticDuplicatesAreReduced() {

        val file = """

            <ASTATE> TOKEN: {
              <FOO: "foo">
            }


            void Bar(): {}
            {
              "bar" <FOO>
            }

            void Bouse(): {}
            {
              "bar" <FOO>
            }

        """.trimIndent().asJccGrammar()

        val lexicalGrammar = file.lexicalGrammar

        lexicalGrammar.lexicalStates should haveSize(2)

        val explicitFoo = file.globalTokenSpecs.first().also { check(it.name == "FOO") }
        val implicitBar = file.descendantSequence().first { it.text == "\"bar\"" }.let { it as JccRegexpExpansionUnit }

        lexicalGrammar.getLexicalState(LexicalState.DefaultStateName)!!.tokens.shouldContainExactly(
            SyntheticToken(implicitBar)
            // not the second one!
        )

        lexicalGrammar.getLexicalState("ASTATE")!!.tokens.shouldContainExactly(
            ExplicitToken(explicitFoo)
        )

    }


    fun testMultipleStates() {

        val file = """

            <ASTATE> TOKEN: {
              <FOO: "foo">
            }

            <*> TOKEN: {
              <BAR: "bar">
            }

            void Bar(): {}
            {
              "bar" <FOO>
            }

        """.trimIndent().asJccGrammar()

        val lexicalGrammar = file.lexicalGrammar

        lexicalGrammar.lexicalStates should haveSize(2)

        val explicitFoo = file.globalTokenSpecs.first().also { check(it.name == "FOO") }
        val explicitBar = file.globalTokenSpecs.drop(1).first().also { check(it.name == "BAR") }

        lexicalGrammar.getLexicalState(LexicalState.DefaultStateName)!!.tokens.shouldContainExactly(
            ExplicitToken(explicitBar)
        )

        lexicalGrammar.getLexicalState("ASTATE")!!.tokens.shouldContainExactly(
            ExplicitToken(explicitFoo),
            ExplicitToken(explicitBar)
        )

    }


}