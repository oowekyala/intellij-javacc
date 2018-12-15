package com.github.oowekyala.ijcc.util.settings

import com.github.oowekyala.ijcc.util.settings.JavaccProjectSettingsService.JccSettingsState
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.Configurable
import org.intellij.lang.annotations.Language
import java.awt.event.ActionEvent
import javax.swing.*

/**
 * @author Clément Fournier
 * @since 1.0
 */
class PluginSettingsPage(initialState: JccSettingsState) : Disposable {

    private val levelToButton = mutableMapOf<InjectionSupportLevel, AbstractButton>()
    var mainPanel : JPanel? = null
    private var fullInjectionRadioButton: JRadioButton? = null
    private var injectionLevelDescriptionLabel: JLabel? = null
    private var conservativeInjectionRadioButton: JRadioButton? = null
    private var injectionDisabledRadioButton: JRadioButton? = null
    private var myInjectionLevel: InjectionSupportLevel = initialState.injectionSupportLevel

    init {
        val injectionLevelGroup = ButtonGroup()

        injectionLevelDescriptionLabel!!.text = injectionLabelText

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


    companion object {

        @Language("HTML")
        private val injectionLabelText = """
        <html>
            Tune the level of sophistication of the Java injection in Java code fragments embedded in a grammar.
            By default this is set to <b>${JavaccProjectSettingsService.defaultInjectionSupportLevel.displayName}</b>,
            which offers great code insight for a reasonable performance trade-off. The level <b>${InjectionSupportLevel.FULL.displayName}</b>
            works correctly, but highlighting usually lags behind code edits, which may be annoying. For more info
            about each level see the tooltips.
        </html>
    """.trimIndent()

    }
}