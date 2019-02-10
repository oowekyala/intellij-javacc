package com.github.oowekyala.ijcc.settings

import com.github.oowekyala.ijcc.settings.JavaccProjectSettingsService.JccSettingsState
import com.intellij.openapi.Disposable
import org.intellij.lang.annotations.Language
import java.awt.event.ActionEvent
import javax.swing.*

/**
 * @author Clément Fournier
 * @since 1.0
 */
class PluginSettingsPage(initialState: JccSettingsState) : Disposable {

    private val levelToButton = mutableMapOf<InjectionSupportLevel, AbstractButton>()

    lateinit var mainPanel: JPanel
    private lateinit var fullInjectionRadioButton: JRadioButton
    private lateinit var injectionLevelDescriptionLabel: JLabel
    private lateinit var conservativeInjectionRadioButton: JRadioButton
    private lateinit var injectionDisabledRadioButton: JRadioButton

    private var myInjectionLevel: InjectionSupportLevel = initialState.injectionSupportLevel

    init {
        val injectionLevelGroup = ButtonGroup()

        injectionLevelDescriptionLabel.text = injectionLabelText

        injectionLevelGroup.add(conservativeInjectionRadioButton)
        injectionLevelGroup.add(fullInjectionRadioButton)
        injectionLevelGroup.add(injectionDisabledRadioButton)

        bindButtonToLanguageLevel(fullInjectionRadioButton, InjectionSupportLevel.FULL)
        bindButtonToLanguageLevel(conservativeInjectionRadioButton, InjectionSupportLevel.CONSERVATIVE)
        bindButtonToLanguageLevel(injectionDisabledRadioButton, InjectionSupportLevel.DISABLED)


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
            This is an <b>experimental</b> feature since 1.1, since it significantly impacts responsiveness of
            e.g. inspections. Because of that, it's disabled by default. You can still enable it if you want, which I
            recommend anyway if you're only editing Java fragments. The level <b>${InjectionSupportLevel.CONSERVATIVE.displayName}</b>
            already offers great code insight features. The level <b>${InjectionSupportLevel.FULL.displayName}</b>
            works correctly, but highlighting usually lags behind code edits, which may be annoying. For more info
            about each level see the tooltips.
        </html>
    """.trimIndent()

    }
}