package com.github.oowekyala.ijcc

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile

/**
 * Context for live templates.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccTemplateContext : TemplateContextType(JavaccLanguage.id, "JavaCC") {
    override fun isInContext(file: PsiFile, offset: Int): Boolean = file.name.matches(Regex(".*[.]jjt?$"))
}