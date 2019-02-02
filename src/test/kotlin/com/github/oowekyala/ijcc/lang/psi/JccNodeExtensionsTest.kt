package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createRegexSpec
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccNodeExtensionsTest : JccTestBase() {


    fun `test JccRegexpSpec isPrivate`() {

        val spec = createRegexSpec(project, RegexKind.TOKEN, "<#FOO: \"f\" >")

        spec.isPrivate shouldBe true


    }
}
