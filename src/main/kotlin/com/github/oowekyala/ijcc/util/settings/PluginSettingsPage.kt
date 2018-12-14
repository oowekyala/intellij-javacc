package com.github.oowekyala.ijcc.util

import com.github.oowekyala.ijcc.util.settings.InjectionSupportLevel
import com.intellij.openapi.Disposable
import java.awt.event.ActionEvent
import com.github.oowekyala.ijcc.util.settings.JavaccProjectSettingsService.JccSettingsState
import javax.swing.*

/**
 * @author Clément Fournier
 * @since 1.0
 */
class PluginSettingsPage(initialState: JccSettingsState) : Disposable {

    private val levelToButton = mutableMapOf<InjectionSupportLevel, AbstractButton>()
    var mainPanel : JPanel? = null
    private var fullInjectionRadioButton: JRadioButton? = null
    private var conservativeInjectionRadioButton: JRadioButton? = null
    private var injectionDisabledRadioButton: JRadioButton? = null
    private var myInjectionLevel: InjectionSupportLevel = initialState.injectionSupportLevel

    init {
        val injectionLevelGroup = ButtonGroup()

        injectionLevelGroup.add(conservativeInjectionRadioButton)
        injectionLevelGroup.add(fullInjectionRadioButton)
        injectionLevelGroup.add(injectionDisabledRadioButton)

        bindButtonToLanguageLevel(fullInjectionRadioButton!!, InjectionSupportLevel.FULL)
        bindButtonToLanguageLevel(conservativeInjectionRadioButton!!, InjectionSupportLevel.CONSERVATIVE)
        bindButtonToLanguageLevel(injectionDisabledRadioButton!!, InjectionSupportLevel.DISABLED)


        injectionLevelGroup.setSelected(levelToButton[myInjectionLevel]!!.model, true)

    }


    private fun bindButtonToLanguageLevel(button: AbstractButton, level: InjectionSupportLevel) {

        button.action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                myInjectionLevel = level
            }
        }

        button.toolTipText = level.description
        button.text = level.displayName

        levelToButton[level] = button
    }


    fun toState(): JccSettingsState {
        return JccSettingsState(myInjectionLevel)
    }

    override fun dispose() {

    }
}