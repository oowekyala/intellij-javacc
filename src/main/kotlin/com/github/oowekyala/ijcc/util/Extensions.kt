package com.github.oowekyala.ijcc.util

import org.jetbrains.annotations.Contract
import java.util.*

/** Select only those elements that are of type R. */
inline fun <reified R> Sequence<*>.filterMapAs(): Sequence<R> =
        this.filter { it is R }.map { it as R }

fun runCatchAll(block: () -> Unit) {
    try {
        block()
    } catch (t: Throwable) {

    }
}

inline fun <T> T.runIt(block: (T) -> Unit) = block(this)

/** Insert [sub] into this string s.t. [sub] is at index [offset] in the resulting string. */
@Contract(pure = true)
fun String.insert(offset: Int, sub: String): String = when {
    offset >= length -> throw IndexOutOfBoundsException()
    this.isEmpty()   -> sub
    sub.isEmpty()    -> this
    else             -> substring(0, offset) + sub + substring(offset, length)
}


operator fun StringBuilder.plusAssign(any: Any) {
    this.append(any)
}

/** Pops the [n] first elements of the stack. */
fun <T> Deque<T>.pop(n: Int): List<T> {
    if (n < 1 || n > size) throw IndexOutOfBoundsException()
    if (n == 1) return listOf(pop())

    var i = n
    val result = mutableListOf<T>()
    while (i-- > 0) {
        result += pop()
    }

    return result
}


// lulz
operator fun Unit.invoke(): Unit = Unit()()()()()()()()

inline fun Boolean.ifTrue(block: () -> Unit): Boolean {
    if (this) {
        block()
    }
    return this
}

inline fun Boolean.ifNot(block: () -> Unit) {
    if (!this) {
        block()
    }
}


fun <T> Iterable<T>.foreachAndBetween(delim: () -> Unit, main: (T) -> Unit) {
    val iterator = iterator()

    if (iterator.hasNext())
        main(iterator.next())
    while (iterator.hasNext()) {
        delim()
        main(iterator.next())
    }
}

fun String.indent(level: Int) = prependIndent("    ".repeat(level))

