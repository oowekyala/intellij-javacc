package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.ijcc.settings.JavaccProjectSettingsService
import com.github.oowekyala.ijcc.settings.javaccSettings
import com.intellij.psi.NavigatablePsiElement

/**
 * Top-level interface for all javacc psi element.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccPsiElement : NavigatablePsiElement {

    override fun getContainingFile(): JccFile

    /** Gets the options bundle associated with the grammar this element is found in. */
    @JvmDefault
    val grammarOptions: InlineGrammarOptions
        get() = containingFile.grammarOptions

    /** Gets the project-specific settings of the plugin. */
    @JvmDefault
    val pluginSettings: JavaccProjectSettingsService
        get() = project.javaccSettings

}
