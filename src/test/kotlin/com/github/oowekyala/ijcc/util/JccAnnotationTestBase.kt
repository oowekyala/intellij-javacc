package com.github.oowekyala.ijcc.util

import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.github.oowekyala.ijcc.settings.InjectionSupportLevel
import com.github.oowekyala.ijcc.settings.javaccSettings
import org.intellij.lang.annotations.Language

/**
 * Inspired from intellij-rust.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccAnnotationTestBase : JccTestBase() {
    protected fun doTest(vararg additionalFilenames: String) {
        myFixture.testHighlighting(fileName, *additionalFilenames)
    }

    protected fun checkHighlighting(@Language("Rust") text: String) =
        checkByText(
            text,
            checkWarn = false,
            checkWeakWarn = false,
            checkInfo = true,
            ignoreExtraHighlighting = false
        )

    protected fun checkInfo(@Language("JavaCC") text: String) =
        checkByText(text, checkWarn = false, checkWeakWarn = false, checkInfo = true)

    protected fun checkWarnings(@Language("JavaCC") text: String) =
        checkByText(text, checkWarn = true, checkWeakWarn = true, checkInfo = false)

    protected fun checkErrors(@Language("JavaCC") text: String) =
        checkByText(text, checkWarn = false, checkWeakWarn = false, checkInfo = false)

    protected fun checkByText(
        @Language("JavaCC") text: String,
        checkWarn: Boolean = true,
        checkInfo: Boolean = false,
        checkWeakWarn: Boolean = false,
        ignoreExtraHighlighting: Boolean = false
    ) = check(
        text,
        checkWarn = checkWarn,
        checkInfo = checkInfo,
        checkWeakWarn = checkWeakWarn,
        ignoreExtraHighlighting = ignoreExtraHighlighting,
        configure = this::configureByText
    )

    protected fun checkFixByText(
        fixName: String,
        @Language("JavaCC") before: String,
        @Language("JavaCC") after: String,
        checkWarn: Boolean = true,
        checkInfo: Boolean = false,
        checkWeakWarn: Boolean = false

    ) = checkFix(
        fixName, before, after,
        configure = this::configureByText,
        checkBefore = { myFixture.checkHighlighting(checkWarn, checkInfo, checkWeakWarn) },
        checkAfter = this::checkByText

    )

    protected fun checkFixByTextWithoutHighlighting(
        fixName: String,
        @Language("JavaCC") before: String,
        @Language("JavaCC") after: String
    ) = checkFix(
        fixName, before, after,
        configure = this::configureByText,
        checkBefore = {},
        checkAfter = this::checkByText

    )

    protected fun checkFixIsUnavailable(
        fixName: String,
        @Language("JavaCC") text: String,
        checkWarn: Boolean = true,
        checkInfo: Boolean = false,
        checkWeakWarn: Boolean = false

    ) = checkFixIsUnavailable(
        fixName, text,
        checkWarn = checkWarn,
        checkInfo = checkInfo,
        checkWeakWarn = checkWeakWarn,
        configure = this::configureByText

    )

    private fun <T> check(
        @Language("JavaCC") text: String,
        checkWarn: Boolean,
        checkInfo: Boolean,
        checkWeakWarn: Boolean,
        ignoreExtraHighlighting: Boolean,
        configure: (String) -> T

    ) {
        disableInjection()
        configure(text)
        myFixture.checkHighlighting(checkWarn, checkInfo, checkWeakWarn, ignoreExtraHighlighting)
    }

    private fun disableInjection() {
        val settingsState = myFixture.project.javaccSettings.myState
        myFixture.project.javaccSettings.myState =
            settingsState.copy(injectionSupportLevel = InjectionSupportLevel.DISABLED)
    }

    private fun <T> checkFix(
        fixName: String,
        @Language("JavaCC") before: String,
        @Language("JavaCC") after: String,
        configure: (String) -> T,
        checkBefore: () -> Unit,
        checkAfter: (String) -> Unit

    ) {
        configure(before)
        checkBefore()
        applyQuickFix(fixName)
        checkAfter(after)
    }

    private fun <T> checkFixIsUnavailable(
        fixName: String,
        @Language("JavaCC") text: String,
        checkWarn: Boolean,
        checkInfo: Boolean,
        checkWeakWarn: Boolean,
        ignoreExtraHighlighting: Boolean = false,
        configure: (String) -> T

    ) {
        check(text, checkWarn, checkInfo, checkWeakWarn, ignoreExtraHighlighting, configure)
        check(myFixture.filterAvailableIntentions(fixName).isEmpty()) {
            "Fix $fixName should not be possible to apply."
        }
    }

    private fun checkByText(text: String) {
        myFixture.checkResult(replaceCaretMarker(text.trimIndent()))
    }


    private fun annot(content: String, desc: String, tagName: String) =
        desc.replace("\"", "\\\"").let { escapedDesc ->
            "<$tagName descr=\"$escapedDesc\">$content</$tagName>"
        }

    fun warningAnnot(s: String, desc: String) = annot(s, desc, "warning")
    fun errorAnnot(s: String, desc: String) = annot(s, desc, "error")
    fun infoAnnot(s: String, desc: String) = annot(s, desc, "info")

}
