package com.github.oowekyala.ijcc.util

import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import java.util.stream.Stream

/** Select only those elements that are of type R. */
inline fun <reified R> Stream<*>.filterMapAs(): Stream<R> = this.filter { it is R }.map { it as R }

val PsiElement.prevSiblingNoWhitespace: PsiElement
    inline get() {
        var sibling = prevSibling
        while (sibling.node.elementType == TokenType.WHITE_SPACE)
            sibling = sibling.prevSibling

        return sibling
    }

// this is just playing around

typealias Fun<A, B> = (A) -> B

infix fun <A, B, C> Fun<A, B>.then(f: Fun<B, C>): Fun<A, C> = { f(this(it)) }

fun <A, B> A.then(f: Fun<A, B>): B = f(this)
