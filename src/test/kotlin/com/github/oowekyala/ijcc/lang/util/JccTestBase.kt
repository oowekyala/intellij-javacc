package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase

/**
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccTestBase : LightCodeInsightFixtureTestCase() {

    protected fun String.asExpansion(): JccExpansion = JccElementFactory.createBnfExpansion(project, this)

    protected fun String.asJccFile(): JccFile = JccElementFactory.createFile(project, this)


    protected fun String.asJccGrammar(): JccFile =
            JccElementFactory.createFile(project, "$DummyHeader$this")

    companion object {

        const val DummyHeader =
                """
PARSER_BEGIN(Dummy)

PARSER_END(Dummy)
"""

    }

}