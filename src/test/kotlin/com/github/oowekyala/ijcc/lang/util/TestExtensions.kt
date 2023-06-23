package com.github.oowekyala.ijcc.lang.util

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.should as ktShould
import io.kotest.matchers.shouldBe as ktShouldBe


fun <T, C : Collection<T>> C?.eachShouldMatchInOrder(vararg assertions: (T) -> Unit) {

    this shouldNotBe null

    this!! // just to inform the type system


    this.zip(assertions).forEach { (item, matcher) ->
        matcher(item)
    }
    this.size ktShouldBe assertions.size
}


fun <T, C : Collection<T>> C?.eachShouldMatchInOrder(vararg matchers: Matcher<T>) {

    this shouldNotBe null

    this!! // just to inform the type system

    this.size ktShouldBe matchers.size

    this.zip(matchers).forEach { (item, matcher) ->
        item ktShould matcher
    }
}

fun String.normalizeWhiteSpace(): String = replace(Regex("""(\s|\R)+"""), " ").trim()

fun <A, B> Matcher<A>.map(f: (B) -> A): Matcher<B> = object : Matcher<B> {
    override fun test(value: B): MatcherResult = this@map.test(f(value))
}


inline fun stringMatchersIgnoreWhitespace(assertions: IgnoreWhitespaceCtx.() -> Unit): Unit =
    IgnoreWhitespaceCtx().assertions()

/** Rebinds match methods for strings to something that normalizes whitespace before handling. */
class IgnoreWhitespaceCtx {


    infix fun String?.should(matcher: Matcher<String?>) {
        this?.normalizeWhiteSpace() ktShould matcher.map { it?.normalizeWhiteSpace() }
    }

    infix fun String?.shouldBe(value: String?) {
        this?.normalizeWhiteSpace() ktShouldBe value?.normalizeWhiteSpace()
    }

}
