package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.util.AssertionMatcher
import com.github.oowekyala.ijcc.lang.util.PsiSpec
import com.github.oowekyala.ijcc.lang.util.matchPsi
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import io.kotlintest.should

/**
 * @author Clément Fournier
 * @since 1.0
 */


open class ParserTestDsl : LightCodeInsightFixtureTestCase() {


    protected fun String.asExpansion(): JccExpansion = JccElementFactory.createBnfExpansion(project, this)

    protected inline fun <reified N : PsiElement> matchExpansion(ignoreChildren: Boolean = false,
                                                                 noinline nodeSpec: PsiSpec<N>): AssertionMatcher<String> =
            {
                it.asExpansion() should matchPsi(ignoreChildren, nodeSpec)
            }
}

