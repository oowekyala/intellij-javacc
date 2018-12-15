package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JavaccPsiElement
import com.github.oowekyala.ijcc.settings.InjectionSupportLevel
import com.intellij.codeInsight.daemon.impl.analysis.HighlightVisitorImpl
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiResolveHelper

/**
 * Highlighter for java code fragments.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class InjectedJavaHighlightVisitor(private val resolveHelper: PsiResolveHelper) : HighlightVisitorImpl(resolveHelper) {

    override fun suitableForFile(file: PsiFile): Boolean =
            InjectedLanguageManager.getInstance(file.project).let { manager ->
                manager.isInjectedFragment(file) && manager.getInjectionHost(file).let { host ->
                    host is JavaccPsiElement && host.pluginSettings.injectionSupportLevel == InjectionSupportLevel.FULL
                }
            }

    override fun clone(): HighlightVisitorImpl = InjectedJavaHighlightVisitor(resolveHelper)

}