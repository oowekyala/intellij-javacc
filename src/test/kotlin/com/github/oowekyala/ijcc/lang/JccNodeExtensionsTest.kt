package com.github.oowekyala.ijcc.lang

import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.JccContainerRegularExpression
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.lang.psi.isPrivate
import com.github.oowekyala.ijcc.lang.psi.isUnclosed
import io.kotlintest.shouldBe
import org.junit.Test

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccNodeExtensionsTest : ParserTestDsl() {


    @Test
    fun `test JccRegexSpec isPrivate`() {

        val spec = project.jccEltFactory.createRegexSpec(RegexKind.TOKEN, "<#FOO: \"f\" >")

        spec.isPrivate shouldBe true

    }

    @Test
    fun `test unclosed container regex`() {

        "<".asRegex<JccContainerRegularExpression>().isUnclosed shouldBe true
        "<\"f\"".asRegex<JccContainerRegularExpression>().isUnclosed shouldBe true
        "<\"f\">".asRegex<JccContainerRegularExpression>().isUnclosed shouldBe false

    }
}
