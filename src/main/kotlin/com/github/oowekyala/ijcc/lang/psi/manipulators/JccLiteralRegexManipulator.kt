package com.github.oowekyala.ijcc.lang.psi.manipulators

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexpUnit
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
class JccLiteralRegexManipulator : AbstractElementManipulator<JccLiteralRegexpUnit>() {
    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(
        regex: JccLiteralRegexpUnit,
        range: TextRange,
        newContent: String
    ): JccLiteralRegexpUnit? {
        val oldText = regex.stringLiteral.text
        val newText = oldText.substring(0, range.startOffset) + newContent +
                oldText.substring(range.endOffset)
        return regex.replace(
            JccElementFactory.createLiteralRegexUnit(
                regex.project,
                newText
            )
        ) as JccLiteralRegexpUnit?
    }
}
