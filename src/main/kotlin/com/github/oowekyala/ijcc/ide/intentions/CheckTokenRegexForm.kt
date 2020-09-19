// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.oowekyala.ijcc.ide.intentions

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.Alarm
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.intellij.lang.regexp.RegExpMatchResult
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author Konstantin Bulenkov
 */
class CheckTokenRegexForm(private val regex: Regex,
                          private val title: String,
                          private val project: Project) {
    private lateinit var mySampleText: EditorTextField
    private lateinit var titleLabel: JLabel
    private lateinit var myRootPanel: JPanel
    private lateinit var myMessage: JBLabel

    private fun createUIComponents() {

        val sampleText: String =
            PropertiesComponent.getInstance(project).getValue(LAST_EDITED_REGEXP, "Sample Text")

        mySampleText = object : EditorTextField(sampleText, project, PlainTextFileType.INSTANCE) {
            override fun createEditor(): EditorEx {
                val editor = super.createEditor()
                editor.isEmbeddedIntoDialogWrapper = true
                return editor
            }

            override fun updateBorder(editor: EditorEx) {
                setupBorder(editor)
            }
        }
        mySampleText.setOneLineMode(false)

        titleLabel = JLabel("<html>$title")


        myRootPanel = object : JPanel(BorderLayout()) {
            var disposable: Disposable? = null
            var updater: Alarm? = null
            override fun addNotify() {
                super.addNotify()
                disposable = Disposer.newDisposable()
                IdeFocusManager.getGlobalInstance().requestFocus(mySampleText, true)
                updater = Alarm(Alarm.ThreadToUse.POOLED_THREAD, disposable)
                val documentListener: DocumentListener =
                    object : DocumentListener {
                        override fun documentChanged(e: DocumentEvent) {
                            update()
                        }
                    }
                mySampleText.addDocumentListener(documentListener)
                update()
                mySampleText.selectAll()
            }

            fun update() {

                val modalityState = ModalityState.defaultModalityState()
                updater!!.cancelAllRequests()
                if (!updater!!.isDisposed) {
                    updater!!.addRequest(
                        {
                            val result = isMatchingText(regex, mySampleText.text)
                            invokeLater(modalityState) {
                                setBalloonState(result)
                            }
                        }, 200
                    )
                }
            }

            override fun removeNotify() {
                super.removeNotify()
                Disposer.dispose(disposable!!)
                PropertiesComponent.getInstance(project).setValue(LAST_EDITED_REGEXP, mySampleText.text)
            }
        }
        myRootPanel.border = JBUI.Borders.empty(
            UIUtil.DEFAULT_VGAP,
            UIUtil.DEFAULT_HGAP
        )
    }

    fun setBalloonState(result: RegExpMatchResult) {
        mySampleText.background =
            if (result == RegExpMatchResult.MATCHES) BACKGROUND_COLOR_MATCH else BACKGROUND_COLOR_NOMATCH
        when (result) {
            RegExpMatchResult.MATCHES    -> myMessage.text = "Matches!"
            RegExpMatchResult.NO_MATCH   -> myMessage.text = "No match"
            RegExpMatchResult.TIMEOUT    -> myMessage.text = "Pattern is too complex"
            RegExpMatchResult.BAD_REGEXP -> myMessage.text = "Bad pattern"
            RegExpMatchResult.INCOMPLETE -> myMessage.text = "More input expected"
            else                         -> throw AssertionError()
        }
        myRootPanel.revalidate()
        val balloon = JBPopupFactory.getInstance().getParentBalloonFor(myRootPanel)
        if (balloon?.isDisposed == false)
            balloon.revalidate()
    }

    val rootPanel: JPanel
        get() = myRootPanel

    companion object {
        private const val LAST_EDITED_REGEXP = "jcc.last.regexp.sample"


        private val BACKGROUND_COLOR_MATCH = JBColor(0xe7fadb, 0x445542)
        private val BACKGROUND_COLOR_NOMATCH = JBColor(0xffb1a0, 0x6e2b28)

        fun isMatchingText(regex: Regex, sampleText: String): RegExpMatchResult =
            ReadAction.compute<RegExpMatchResult, RuntimeException> {
                try {
                    if (regex.matches(StringUtil.newBombedCharSequence(sampleText, 1000)))
                        RegExpMatchResult.MATCHES
                    else
                        RegExpMatchResult.NO_MATCH
                } catch (ignore: ProcessCanceledException) {
                    RegExpMatchResult.TIMEOUT
                } catch (ignore: Exception) {
                    RegExpMatchResult.BAD_REGEXP
                }
            }
    }

}
