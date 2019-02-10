package com.github.oowekyala.ijcc.lang.psi.manipulators

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexUnit
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createRegexElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException

/**
 * Manipulator for literal regexes.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccLiteralRegexManipulator : AbstractElementManipulator<JccLiteralRegexUnit>() {
    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(
        regex: JccLiteralRegexUnit,
        range: TextRange,
        newContent: String
    ): JccLiteralRegexUnit? {
        val oldText = regex.stringLiteral.text
        val newText = oldText.substring(0, range.startOffset) + newContent +
            oldText.substring(range.endOffset)
        return regex.replace(
            createRegexElement<JccLiteralRegexUnit>(
                regex.project,
                newText
            )
        ) as JccLiteralRegexUnit?
    }
}
