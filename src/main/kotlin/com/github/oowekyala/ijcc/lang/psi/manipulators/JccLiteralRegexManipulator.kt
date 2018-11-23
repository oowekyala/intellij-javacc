package com.github.oowekyala.ijcc.lang.psi.manipulators

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegularExpression
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException

/**
 * Manipulator for literal regexes.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccLiteralRegexManipulator : AbstractElementManipulator<JccLiteralRegularExpression>() {
    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(
        regex: JccLiteralRegularExpression,
        range: TextRange,
        newContent: String
    ): JccLiteralRegularExpression? {
        val oldText = regex.stringLiteral.text
        val newText = oldText.substring(0, range.startOffset) + newContent +
                oldText.substring(range.endOffset)
        return regex.replace(
            JccElementFactory.createLiteralRegex(
                regex.project,
                newText
            )
        ) as JccLiteralRegularExpression?
    }
}
