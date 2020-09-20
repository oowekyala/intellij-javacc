package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.lang.psi.JccPsiElement
import com.github.oowekyala.ijcc.settings.InjectionSupportLevel
import com.github.oowekyala.ijcc.settings.pluginSettings
import com.intellij.codeInsight.daemon.impl.analysis.HighlightVisitorImpl
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiFile

/**
 * Highlighter for java code fragments.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class InjectedJavaHighlightVisitor : HighlightVisitorImpl() {

    override fun suitableForFile(file: PsiFile): Boolean =
        InjectedLanguageManager.getInstance(file.project).let { manager ->
            manager.isInjectedFragment(file) && manager.getInjectionHost(file).let { host ->
                host is JccPsiElement && host.pluginSettings.injectionSupportLevel == InjectionSupportLevel.FULL
            }
        }

    override fun clone(): HighlightVisitorImpl =
        InjectedJavaHighlightVisitor()

}
