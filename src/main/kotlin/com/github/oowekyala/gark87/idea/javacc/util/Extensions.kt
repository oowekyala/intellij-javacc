package com.github.oowekyala.gark87.idea.javacc.util

/**
 * @author Clément Fournier
 * @since 1.0
 */

inline fun <reified R> Array<*>.filterByType(): List<R> = this.filter { it is R }.map { it as R }

