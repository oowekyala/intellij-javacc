package com.github.oowekyala.ijcc.lang.psi.stubs

import com.intellij.psi.stubs.StubElement

/**
 * @author Clément Fournier
 * @since 1.2
 */


fun StubElement<*>.ancestors(includeSelf: Boolean): Sequence<StubElement<*>> =
    generateSequence(if (includeSelf) this else parentStub) { it.parentStub }