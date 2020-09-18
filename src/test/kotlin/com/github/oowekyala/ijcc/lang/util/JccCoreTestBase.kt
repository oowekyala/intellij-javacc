package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.JjtxCoreEnvironment
import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpression
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.intellij.openapi.project.Project
import org.intellij.lang.annotations.Language

/**
 * Base class offering utilities to parse and create tests.
 * Mostly copied from intellij-JavaCC.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccCoreTestBase : ParseUtilsMixin {

    private val coreEnv = JjtxCoreEnvironment.createTestEnvironment()


    inline fun <reified R : JccExpansion> String.asExpansionOfType(): R =
        asExpansion().also { check(it is R) }.let { it as R }

    inline fun <reified R : JccRegularExpression> String.asRegex(): R =
        getProject().jccEltFactory.createRegex(this)


    override fun getProject(): Project = coreEnv.project


    companion object {

        @Language("JavaCC")
        const val DummyHeader = // changing spaces on that may break tests, don't do
            """
PARSER_BEGIN(Dummy)

package dummy.grammar;

public class Dummy {

}

PARSER_END(Dummy)
"""

        @JvmStatic
        fun camelOrWordsToSnake(name: String): String {
            if (' ' in name) return name.trim().replace(" ", "_")

            return name.split("(?=[A-Z])".toRegex()).joinToString("_", transform = String::toLowerCase)
        }
    }

}

val JccCoreTestBase.project
    get() = getProject()
