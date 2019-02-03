package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.psi.JccTypesExt
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.ex.BaseLocalInspectionTool
import com.intellij.psi.JavaTokenType.C_STYLE_COMMENT
import com.intellij.psi.JavaTokenType.END_OF_LINE_COMMENT
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.JavaDocElementType

/**
 * Base class for inspections.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccInspectionBase(private val myDisplayName: String)
    : BaseLocalInspectionTool() {
    override fun getID(): String = "JavaCC$shortName"
    override fun getDisplayName(): String = myDisplayName
    override fun getShortName(): String = InspectionProfileEntry.getShortName(this::class.java.simpleName)
    override fun getGroupDisplayName(): String = JavaccLanguage.displayName

}

val PsiElement.isJccComment: Boolean
    get() = JccTypesExt.CommentTypeSet.contains(node.elementType)

val PsiElement.trimCommentMarkers: String
    get() = when (node.elementType) {
        END_OF_LINE_COMMENT            -> text.removePrefix("//")
        C_STYLE_COMMENT                -> text.removeSurrounding("/*", "*/")
        JavaDocElementType.DOC_COMMENT -> text.removeSurrounding("/**", "*/")
        else                           -> text
    }
