package com.github.oowekyala.ijcc.util

import com.intellij.util.containers.ContainerUtil
import org.jetbrains.annotations.Contract
import java.util.*

/** Select only those elements that are of type R. */
inline fun <reified R> Sequence<*>.filterMapAs(): Sequence<R> =
        this.filter { it is R }.map { it as R }


/** Like [run], but doesn't use a lambda with receiver. */
inline fun <T> T.runIt(block: (T) -> Unit) {
    block(this)
}

/** Insert [sub] into this string s.t. [sub] is at index [offset] in the resulting string. */
@Contract(pure = true)
fun String.insert(offset: Int, sub: String): String = when {
    offset >= length || offset < 0 -> throw IndexOutOfBoundsException()
    this.isEmpty()                 -> sub
    sub.isEmpty()                  -> this
    else                           -> substring(0, offset) + sub + substring(offset, length)
}


operator fun StringBuilder.plusAssign(any: Any) {
    this.append(any)
}

/** Pops the [n] first elements of the stack. */
fun <T> Deque<T>.pop(n: Int): List<T> {
    if (n < 0 || n > size) throw IndexOutOfBoundsException()
    if (n == 0) return emptyList()
    if (n == 1) return listOf(pop())

    var i = n
    val result = mutableListOf<T>()
    while (i-- > 0) {
        result += pop()
    }

    return result.asReversed()
}


private object O {
    operator fun invoke(o: Any = O): O =
            (((((O)))))(S)(E)(N)(D)(((((O)))))(N)(U)(D)(E)(S)(((((O)))))
}
private object N
private object U
private object D
private object E
private object S


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

fun <T> MutableList<T>.removeLast(): T = removeAt(lastIndex)


fun <T : Any> MutableCollection<T>.addIfNotNull(t: T?) = ContainerUtil.addIfNotNull(this, t)