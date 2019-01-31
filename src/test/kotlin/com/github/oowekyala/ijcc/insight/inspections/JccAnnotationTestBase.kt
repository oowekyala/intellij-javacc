package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.github.oowekyala.ijcc.settings.InjectionSupportLevel
import com.github.oowekyala.ijcc.settings.JavaccProjectSettingsService
import com.github.oowekyala.ijcc.settings.javaccSettings
import org.intellij.lang.annotations.Language

/**
 * Ripped from intellij-rust.
 *
 * @author Clément Fournier
 * @since 1.0
 */
open class JccAnnotationTestBase : JccTestBase() {
    protected fun doTest(vararg additionalFilenames: String) {
        myFixture.testHighlighting(fileName, *additionalFilenames)
    }

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
        checkWeakWarn: Boolean = false

    ) = check(
        text,
        checkWarn = checkWarn,
        checkInfo = checkInfo,
        checkWeakWarn = checkWeakWarn,
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

    private fun check(
        @Language("JavaCC") text: String,
        checkWarn: Boolean,
        checkInfo: Boolean,
        checkWeakWarn: Boolean,
        configure: (String) -> Unit

    ) {
        disableInjection()
        configure(text)
        myFixture.checkHighlighting(checkWarn, checkInfo, checkWeakWarn)
    }

    private fun disableInjection() {
        val settingsState = myFixture.project.javaccSettings.myState
        myFixture.project.javaccSettings.myState = settingsState.copy(injectionSupportLevel = InjectionSupportLevel.DISABLED)
    }

    private fun checkFix(
        fixName: String,
        @Language("JavaCC") before: String,
        @Language("JavaCC") after: String,
        configure: (String) -> Unit,
        checkBefore: () -> Unit,
        checkAfter: (String) -> Unit

    ) {
        configure(before)
        checkBefore()
        applyQuickFix(fixName)
        checkAfter(after)
    }

    private fun checkFixIsUnavailable(
        fixName: String,
        @Language("JavaCC") text: String,
        checkWarn: Boolean,
        checkInfo: Boolean,
        checkWeakWarn: Boolean,
        configure: (String) -> Unit

    ) {
        check(text, checkWarn, checkInfo, checkWeakWarn, configure)
        check(myFixture.filterAvailableIntentions(fixName).isEmpty()) {
            "Fix $fixName should not be possible to apply."
        }
    }

    private fun checkByText(text: String) {
        myFixture.checkResult(replaceCaretMarker(text.trimIndent()))
    }


}