package com.github.oowekyala.ijcc.settings

import com.github.oowekyala.ijcc.settings.JavaccProjectSettingsService.JccSettingsState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * @author Clément Fournier
 * @since 1.0
 */
@State(name = "JavaccProjectSettings") // use misc.xml
class JavaccProjectSettingsServiceImpl : JavaccProjectSettingsService,
    PersistentStateComponent<JavaccProjectSettingsService.PersistableSettingsState> {

    override var myState = JccSettingsState()

    override fun getState(): JavaccProjectSettingsService.PersistableSettingsState = myState.toMutable()

    override fun loadState(state: JavaccProjectSettingsService.PersistableSettingsState) {
        myState = state.toImmutable()
    }

    // TODO notify bus when the injection level changes?
}