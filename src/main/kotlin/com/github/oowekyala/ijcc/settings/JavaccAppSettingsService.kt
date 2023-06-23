package com.github.oowekyala.ijcc.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * App-level settings of the plugin.
 *
 * @author Clément Fournier
 * @since 1.0
 */
@State(name = "JavaccAppSettings", storages = [Storage("javacc-plugin.xml")])
class JavaccAppSettingsService : PersistentStateComponent<JccGlobalSettingsState> {

    override fun loadState(state: JccGlobalSettingsState) {
        myState = state.copy()
    }

    private var myState = JccGlobalSettingsState()

    override fun getState(): JccGlobalSettingsState = myState

}

/**
 * App-level settings of the plugin.
 */
val globalPluginSettings: JccGlobalSettingsState
    get() = ApplicationManager.getApplication()
        .getService(JavaccAppSettingsService::class.java).state


data class JccGlobalSettingsState(
    var isFoldJavaFragments: Boolean = true,
    var isFoldLookaheads: Boolean = true,
    var isFoldTokenRefs: Boolean = true,

    var isFoldOptions: Boolean = true,
    var isFoldParserDecl: Boolean = true,
    var isFoldTokenMgrDecl: Boolean = true,
    var isFoldTokenProds: Boolean = true,

    var isFoldBgenSections: Boolean = true
) {

    fun setAllFoldingOptsTo(b: Boolean) {
        isFoldJavaFragments = b
        isFoldLookaheads = b
        isFoldTokenRefs = b

        isFoldOptions = b
        isFoldParserDecl = b
        isFoldTokenMgrDecl = b
        isFoldTokenProds = b

        isFoldBgenSections = b
    }

}
