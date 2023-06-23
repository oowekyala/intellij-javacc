package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.psi.JccRegexExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.descendantSequence
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.should

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
        val implicitBar = file.descendantSequence().first { it.text == "\"bar\"" }.let { it as JccRegexExpansionUnit }
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
        val implicitBar = file.descendantSequence().first { it.text == "\"bar\"" }.let { it as JccRegexExpansionUnit }

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
        val implicitBar = file.descendantSequence().first { it.text == "\"bar\"" }.let { it as JccRegexExpansionUnit }

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
