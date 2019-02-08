package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegexSpec
import com.github.oowekyala.ijcc.lang.psi.promoteToRegex
import com.github.oowekyala.ijcc.lang.psi.safeReplace
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

/**
 * @author Clément Fournier
 * @since 1.0
 */
class RemoveNameFromRegexIntention
    : SelfTargetingOffsetIndependentIntention<JccNamedRegularExpression>(
    JccNamedRegularExpression::class.java,
    "Remove name from regex"
), LowPriorityAction {
    override fun applyTo(project: Project, editor: Editor?, element: JccNamedRegularExpression) {
        element.safeReplace(element.regexElement!!.promoteToRegex())
    }

    override fun isApplicableTo(element: JccNamedRegularExpression): Boolean {



        return element.parent !is JccRegexSpec && element.regexElement != null
    }
}