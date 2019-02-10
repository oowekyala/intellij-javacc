package com.github.oowekyala.ijcc.settings

import com.intellij.openapi.components.ServiceManager.getService
import com.intellij.openapi.project.Project

/**
 * Project-level settings of the plugin.
 * See [Project.javaccSettings] to get an instance.
 *
 * The implementation ([JavaccProjectSettingsServiceImpl]) is registered
 * as a `projectService` within `plugin.xml`.
 *
 * The [JavaccProjectSettingsConfigurable] registers the [PluginSettingsPage]
 * in the settings menu. See also the attributes set on those in the plugin
 * manifest.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JavaccProjectSettingsService {

    var myState: JccSettingsState

    val injectionSupportLevel: InjectionSupportLevel
        get() = myState.injectionSupportLevel

    /** Immutable version, the one used within the project. */
    data class JccSettingsState(
        val injectionSupportLevel: InjectionSupportLevel = defaultInjectionSupportLevel
    ) {
        fun toMutable(): PersistableSettingsState = PersistableSettingsState(injectionSupportLevel)


    }

    /** Mutable version, that's the one being serialized since the persistence framework needs setters.. */
    data class PersistableSettingsState(
        var injectionSupportLevel: InjectionSupportLevel = defaultInjectionSupportLevel
    ) {
        fun toImmutable(): JccSettingsState = JccSettingsState(injectionSupportLevel)
    }

    companion object {
        val defaultInjectionSupportLevel: InjectionSupportLevel = InjectionSupportLevel.DISABLED
    }
}

/** Gets the instance of [JavaccProjectSettingsService] for this project. */
val Project.javaccSettings: JavaccProjectSettingsService
    get() = getService(this, JavaccProjectSettingsService::class.java)
        ?: error("Failed to get JavaccProjectSettingsService for $this")