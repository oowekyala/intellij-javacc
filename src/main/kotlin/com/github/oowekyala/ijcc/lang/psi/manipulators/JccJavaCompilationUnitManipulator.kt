package com.github.oowekyala.ijcc.lang.psi.manipulators

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.JccJavaCompilationUnit
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.lang.psi.isOfType
import com.github.oowekyala.ijcc.lang.psi.siblingSequence
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator


/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccJavaCompilationUnitManipulator : AbstractElementManipulator<JccJavaCompilationUnit>() {


    override fun getRangeInElement(element: JccJavaCompilationUnit): TextRange {
        val braceOffset = element.lastChild.siblingSequence(false).firstOrNull { it.isOfType(JccTypes.JCC_RBRACE) } ?: return super.getRangeInElement(element)
        return TextRange.from(0, braceOffset.textRange.startOffset - element.textRange.startOffset)
    }


    override fun handleContentChange(element: JccJavaCompilationUnit,
                                     range: TextRange,
                                     newContent: String?): JccJavaCompilationUnit? {
        val oldText = element.text
        val newText = oldText.substring(0, range.startOffset) + newContent +
            oldText.substring(range.endOffset)
        return element.replace(element.project.jccEltFactory.createJcu(newText)) as JccJavaCompilationUnit
    }
}
