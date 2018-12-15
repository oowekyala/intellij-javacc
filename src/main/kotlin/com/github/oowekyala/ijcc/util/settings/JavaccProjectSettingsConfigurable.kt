package com.github.oowekyala.ijcc.util.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent

/**
 * Manages the plugin settings page.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccProjectSettingsConfigurable(project: Project) : Configurable, Configurable.NoScroll {


    private val javaccSettings = project.javaccSettings

    private val panelCache: PluginSettingsPage by lazy {
        PluginSettingsPage(
            javaccSettings.myState
        )
    }

    override fun isModified(): Boolean = panelCache.toState() != javaccSettings.myState

    override fun getDisplayName(): String = "JavaCC"

    override fun apply() {
        javaccSettings.myState = panelCache.toState()
    }

    override fun disposeUIResources() = Disposer.dispose(panelCache)

    override fun createComponent(): JComponent? = panelCache.mainPanel

}