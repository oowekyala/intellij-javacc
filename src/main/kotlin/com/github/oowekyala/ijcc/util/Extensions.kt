package com.github.oowekyala.ijcc.util

import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType

/** Select only those elements that are of type R. */
inline fun <reified R> Sequence<*>.filterMapAs(): Sequence<R> = this.filter { it is R }.map { it as R }

/** Lazy sequence of children. */
fun PsiElement.childrenSequence(reversed: Boolean = false): Sequence<PsiElement> = when (reversed) {
    false -> children.asSequence()
    true  -> {
        val children = children
        var i = children.size
        sequence {
            while (i > 0) {
                val child = children[--i]
                yield(child)
            }
        }
    }
}


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


val PsiElement.prevSiblingNoWhitespace: PsiElement
    inline get() {
        var sibling = prevSibling
        while (sibling.node.elementType == TokenType.WHITE_SPACE)
            sibling = sibling.prevSibling

        return sibling
    }


val PsiElement.lastChildNoWhitespace: PsiElement?
    inline get() = childrenSequence(reversed = true).firstOrNull { it.node.elementType != TokenType.WHITE_SPACE }

