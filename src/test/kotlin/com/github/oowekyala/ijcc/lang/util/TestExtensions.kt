package com.github.oowekyala.ijcc.lang.util

import arrow.core.Either
import io.kotlintest.*
import io.kotlintest.should as ktShould
import io.kotlintest.shouldBe as ktShouldBe


fun <T, C : Collection<T>> C?.eachShouldMatchInOrder(vararg assertions: (T) -> Unit) {

    this shouldNotBe null

    this!! // just to inform the type system

    this.size ktShouldBe assertions.size

    this.zip(assertions).forEach { (item, matcher) ->
        matcher(item)
    }
}


fun <T, C : Collection<T>> C?.eachShouldMatchInOrder(vararg matchers: Matcher<T>) {

    this shouldNotBe null

    this!! // just to inform the type system

    this.size ktShouldBe matchers.size

    this.zip(matchers).forEach { (item, matcher) ->
        item ktShould matcher
    }
}

/**
 * Turns a set of assertions into a matcher that succeeds if executing the [assertions]
 * doesn't throw any [AssertionError]s.
 */
inline fun <T> verify(crossinline assertions: (T) -> Unit): Matcher<T> = object : Matcher<T> {
    override fun test(value: T): Result {

        val matchRes = try {
            Either.Right(assertions(value))
        } catch (e: AssertionError) {
            Either.Left(e)
        }

        val didMatch = matchRes.isRight()

        val failureMessage: String = matchRes.fold({
            // Here the node failed
            it.message ?: "Did not match the assertions (no cause specified)"
        }, {
            // The node matched, which was expected
            "SHOULD NOT BE OUTPUT"
        })

        val negatedMessage = matchRes.fold({
            // the node didn't match, which was expected
            "SHOULD NOT BE OUTPUT"
        }, {
            "Should have matched the assertions"
        })

        return Result(didMatch, failureMessage, negatedMessage)
    }

}


fun String.normalizeWhiteSpace(): String = replace(Regex("""([\s]|\R)+"""), " ").trim()

fun <A, B> Matcher<A>.map(f: (B) -> A): Matcher<B> = object : Matcher<B> {
    override fun test(value: B): Result = this@map.test(f(value))
}


inline fun stringMatchersIgnoreWhitespace(assertions: IgnoreWhitespaceCtx.() -> Unit): Unit =
        IgnoreWhitespaceCtx().assertions()

/** Rebinds match methods for strings to something that normalizes whitespace before handling. */
class IgnoreWhitespaceCtx {


    infix fun String?.should(matcher: Matcher<in String?>) {
        this?.normalizeWhiteSpace() ktShould matcher.map { it?.normalizeWhiteSpace() }
    }

    infix fun String?.shouldBe(value: String?) {
        this?.normalizeWhiteSpace() ktShouldBe value?.normalizeWhiteSpace()
    }

}