package com.github.oowekyala.ijcc.lang

import com.github.oowekyala.ijcc.lang.util.AssertionMatcher
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.github.oowekyala.ijcc.lang.util.PsiSpec
import com.github.oowekyala.ijcc.lang.util.matchPsi
import com.intellij.psi.PsiElement
import io.kotlintest.should

/**
 * @author Clément Fournier
 * @since 1.0
 */


abstract class ParserTestDsl : JccTestBase() {


    protected inline fun <reified N : PsiElement> matchExpansion(ignoreChildren: Boolean = false,
                                                                 noinline nodeSpec: PsiSpec<N>): AssertionMatcher<String> =
        {
            it.asExpansion() should matchPsi(ignoreChildren, nodeSpec)
        }
}

