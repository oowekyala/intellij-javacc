package com.github.oowekyala.ijcc.util

/** Select only those elements that are of type R. */
inline fun <reified R> Sequence<*>.filterMapAs(): Sequence<R> =
        this.filter { it is R }.map { it as R }

fun runCatchAll(block: () -> Unit) {
    try {
        block()
    } catch (t: Throwable) {

    }
}

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

