package com.github.oowekyala.ijcc.util

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.annotations.Contract
import java.util.*


/** Like [run], but doesn't use a lambda with receiver. */
inline fun <T> T.runIt(block: (T) -> Unit) {
    block(this)
}

fun <T : Any> Sequence<T>.firstOfAnyType(vararg types: Class<out T>): T? =
        first { t -> types.any { type -> type.isInstance(t) } }

/** Insert [sub] into this string s.t. [sub] is at index [offset] in the resulting string. */
@Contract(pure = true)
fun String.insert(offset: Int, sub: String): String = when {
    offset >= length || offset < 0 -> throw IndexOutOfBoundsException()
    this.isEmpty()                 -> sub
    sub.isEmpty()                  -> this
    else                           -> substring(0, offset) + sub + substring(offset, length)
}

inline fun unless(condition: Boolean, block: () -> Unit) {
    if (!condition) block()
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


@Suppress("UNUSED_PARAMETER")
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


fun String.deleteWhitespace(): String = replace(Regex("\\s"), "")

fun <T : Any> MutableCollection<T>.addIfNotNull(t: T?) = ContainerUtil.addIfNotNull(this, t)

/**
 * Like the other overload. The returned sequence contains [t] (the stop element)
 * if [this] sequence contains it.
 */
fun <T> Sequence<T>.takeUntil(t: T): Sequence<T> = takeUntil { it == t }

/**
 * Returns a sequence containing all the elements of this sequence
 * that are before the first element that matches the predicate, and
 * includes that last element.
 *
 * @receiver Must be iterable multiple times.
 */
fun <T> Sequence<T>.takeUntil(pred: (T) -> Boolean): Sequence<T> {
    val fst = indexOfFirst { pred(it) }
    return take(fst + 1)
}


fun <T> Sequence<T>.prepend(t: T): Sequence<T> = sequenceOf(t).plus(this)

fun <A, B, C, D> Pair<A, B>.map(f: (A) -> C, g: (B) -> D): Pair<C, D> = Pair(f(first), g(second))

fun TokenSet.contains(psiElement: PsiElement): Boolean = psiElement.node?.let { this.contains(it.elementType) } == true


fun <T> Comparator<T>.deemsEqual(t1: T, t2: T) = compare(t1, t2) == 0

public inline fun <T, R> Sequence<T?>.foldNullable(initial: R, operation: (acc: R, T) -> R): R? =
        fold(initial as R?) { r, t ->
            if (t == null || r == null) null
            else operation(r, t)
        }

