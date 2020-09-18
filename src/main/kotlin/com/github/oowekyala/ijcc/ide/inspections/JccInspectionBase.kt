package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.JccTypes.JCC_C_STYLE_COMMENT
import com.github.oowekyala.ijcc.lang.psi.JccTypesExt
import com.github.oowekyala.ijcc.lang.JccTypes.JCC_END_OF_LINE_COMMENT
import com.github.oowekyala.ijcc.util.contains
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.JavaDocElementType

/**
 * Base class for inspections.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccInspectionBase(private val myDisplayName: String) : LocalInspectionTool() {
    // Since inspections can be suppressed inspection names and this scheme are published API
    // Basically the class name minus the "Inspection" suffix is the id
    // If the name collides with inspections of other languages, prefix the node class with "Jcc"
    // This is formalized in 1.3
    final override fun getID(): String = InspectionProfileEntry.getShortName(this::class.java.simpleName)

    final override fun getDisplayName(): String = myDisplayName
    final override fun getShortName(): String = id // short name must be unique wrt all other inspections
    final override fun getGroupDisplayName(): String = JavaccLanguage.displayName

    override fun isEnabledByDefault(): Boolean = true
}

val PsiElement.isJccComment: Boolean
    get() = JccTypesExt.CommentTypeSet.contains(this)

val PsiElement.trimCommentMarkers: String
    get() = when (node.elementType) {
        JCC_END_OF_LINE_COMMENT -> text.removePrefix("//")
        JCC_C_STYLE_COMMENT     -> text.removeSurrounding("/*", "*/")
        else                    -> text
    }

