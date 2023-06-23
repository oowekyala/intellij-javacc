package com.github.oowekyala.ijcc.util

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.MostlySingularMultiMap
import org.jetbrains.annotations.Contract
import java.util.*


/** Like [run], but doesn't use a lambda with receiver. */
inline fun <T> T.runIt(block: (T) -> Unit) {
    block(this)
}

fun <T : Any> Sequence<T>.firstOfAnyType(vararg types: Class<out T>): T? =
    firstOrNull { t -> types.any { type -> type.isInstance(t) } }

/** Insert [sub] into this string s.t. [sub] is at indices [offset] in the resulting string. */
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


fun <T> Iterable<T>.foreachAndBetween(delim: () -> Unit, main: (T) -> Unit) {
    val iterator = iterator()

    if (iterator.hasNext())
        main(iterator.next())
    while (iterator.hasNext()) {
        delim()
        main(iterator.next())
    }
}

fun String.indent(level: Int, indentStr: String = "    ") = prependIndent(indentStr.repeat(level))

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
    return if (fst < 0) this else take(fst + 1)
}


fun <T> Sequence<T>.prepend(t: T): Sequence<T> = sequenceOf(t).plus(this)

fun <A, B, C, D> Pair<A, B>.map(f: (A) -> C, g: (B) -> D): Pair<C, D> = Pair(f(first), g(second))

fun TokenSet.contains(psiElement: PsiElement): Boolean = psiElement.node?.let { this.contains(it.elementType) } == true

/**
 * Folds the elements of this sequence, breaking as soon as a null value is encountered or
 * the operation returns null.
 */
inline fun <T, R> Sequence<T?>.foldNullable(initial: R, operation: (acc: R, T) -> R?): R? {
    var accumulator: R = initial
    for (element in this) {
        if (element == null) return null
        else accumulator = operation(accumulator, element) ?: return null
    }
    return accumulator
}


fun <T> List<T>.init(): List<T> =
    if (isEmpty()) throw NoSuchElementException() else subList(0, size - 1)

fun <T> List<T>.tail(): List<T> =
    if (isEmpty()) throw NoSuchElementException() else subList(1, size)

fun String.capitalize(): String =
    replaceFirstChar { it.uppercaseChar() }

// null keys will not be added.
// values must be checked to be not null by client

fun <T, K> Sequence<T>.associateByToMostlySingular(keySelector: (T) -> K?): MostlySingularMultiMap<K, T> =
    associateByToMostlySingular(keySelector) { it }

fun <T, K, V> Sequence<T>.associateByToMostlySingular(
    keySelector: (T) -> K?,
    valueTransform: (T) -> V?
): MostlySingularMultiMap<K, V> {
    val multiMap = MostlySingularMultiMap<K, V>()
    for (element in this) {
        val k = keySelector(element)
        val v = valueTransform(element)
        if (k != null && v != null) {
            multiMap.add(k, v)
        }
    }
    return multiMap
}

fun <K : Any, V : Any> MostlySingularMultiMap<K, V>.firstValues(): Map<K, V> {
    val map = mutableMapOf<K, V>()

    for (k in keySet()) {
        val v = get(k).firstOrNull()
        if (v != null) map[k] = v
    }

    return map
}

fun <K : Any, V : Any> MostlySingularMultiMap<K, V>.asMap(): Map<K, List<V>> {

    val wrapped = this

    return object : Map<K, List<V>> {
        override val entries: Set<Map.Entry<K, List<V>>>
            get() = keys.mapTo(mutableSetOf()) { AbstractMap.SimpleImmutableEntry(it, this[it]!!) }
        override val keys: Set<K>
            get() = wrapped.keySet()
        override val size: Int
            get() = wrapped.size()
        override val values: List<List<V>>
            get() = keys.map { this[it]!! }

        override fun containsKey(key: K): Boolean = wrapped.containsKey(key)

        override fun containsValue(value: List<V>): Boolean = keys.any { get(it) == value }

        override fun get(key: K): List<V>? = wrapped.get(key).toList().takeIf { containsKey(key) }

        override fun isEmpty(): Boolean = wrapped.isEmpty

    }
}

fun <K, V> Map<List<K>, V>.spread(): Map<K, V> {

    val res = mutableMapOf<K, V>()

    for ((lst, v) in this)
        for (k in lst)
            res[k] = v

    return res
}
