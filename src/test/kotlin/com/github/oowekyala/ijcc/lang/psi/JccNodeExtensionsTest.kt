package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.parser.JccParserTests
import com.github.oowekyala.ijcc.lang.parser.ParserTestDsl
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createRegexSpec
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccNodeExtensionsTest : ParserTestDsl() {


    fun `test JccRegexSpec isPrivate`() {

        val spec = createRegexSpec(project, RegexKind.TOKEN, "<#FOO: \"f\" >")

        spec.isPrivate shouldBe true

    }

    fun `test unclosed container regex`() {

        "<".asRegex<JccContainerRegularExpression>().isUnclosed shouldBe true
        "<\"f\"".asRegex<JccContainerRegularExpression>().isUnclosed shouldBe true
        "<\"f\">".asRegex<JccContainerRegularExpression>().isUnclosed shouldBe false

    }
}
