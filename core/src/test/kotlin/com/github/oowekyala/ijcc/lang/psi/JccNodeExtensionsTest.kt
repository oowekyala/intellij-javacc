package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.parser.ParserTestDsl
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccNodeExtensionsTest : ParserTestDsl() {


    fun `test JccRegexSpec isPrivate`() {

        val spec = project.jccEltFactory.createRegexSpec(RegexKind.TOKEN, "<#FOO: \"f\" >")

        spec.isPrivate shouldBe true

    }

    fun `test unclosed container regex`() {

        "<".asRegex<JccContainerRegularExpression>().isUnclosed shouldBe true
        "<\"f\"".asRegex<JccContainerRegularExpression>().isUnclosed shouldBe true
        "<\"f\">".asRegex<JccContainerRegularExpression>().isUnclosed shouldBe false

    }
}
