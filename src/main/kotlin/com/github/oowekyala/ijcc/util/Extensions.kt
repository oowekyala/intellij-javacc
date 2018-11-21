package com.github.oowekyala.ijcc.util

import java.util.stream.Stream

/** Select only those elements that are of type R. */
inline fun <reified R> Stream<*>.filterMapAs(): Stream<R> = this.filter { it is R }.map { it as R }

// this is just playing around

typealias Fun<A, B> = (A) -> B

infix fun <A, B, C> Fun<A, B>.then(f: Fun<B, C>): Fun<A, C> = { f(this(it)) }

fun <A, B> A.then(f: Fun<A, B>): B = f(this)
