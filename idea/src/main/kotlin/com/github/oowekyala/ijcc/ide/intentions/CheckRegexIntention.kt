package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.ide.quickdoc.JccTerminalDocMaker
import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRefRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpression
import com.github.oowekyala.ijcc.lang.psi.pattern
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInsight.intention.impl.QuickEditHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import org.intellij.lang.regexp.RegExpLanguage
import javax.swing.Icon


class CheckRegExpIntentionAction :
    JccSelfTargetingEditorIntentionBase<JccRegularExpression>(
        JccRegularExpression::class.java,
        "Check Regexp"
    ),
    LowPriorityAction, Iconable {


    override fun isApplicableTo(element: JccRegularExpression): Boolean =
        element !is JccRefRegularExpression && element.pattern != null

    override fun run(project: Project, editor: Editor, element: JccRegularExpression): () -> Unit = {

        element.pattern?.runIt { r ->
            val name = JccTerminalDocMaker.htmlNameOfToken(element.name)
            val component = CheckTokenRegexForm(r, name, project).rootPanel

            QuickEditHandler.showBalloon(editor, element.containingFile, component)

        }
    }

    override fun startInWriteAction(): Boolean = false

    override fun getIcon(flags: Int): Icon? = RegExpLanguage.INSTANCE.associatedFileType?.icon
}
