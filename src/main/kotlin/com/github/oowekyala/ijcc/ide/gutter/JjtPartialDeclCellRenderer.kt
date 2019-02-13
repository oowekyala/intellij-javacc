package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccScopedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.ancestors
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * Renders cells in [JjtreePartialDeclarationLineMarkerProvider]
 * popup (name and location string).
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JjtPartialDeclCellRenderer : DefaultPsiElementCellRenderer() {


    override fun getIcon(element: PsiElement?): Icon = JccIcons.JJTREE_NODE

    override fun getElementText(element: PsiElement): String =
        (element as? JjtNodeClassOwner).let { owner ->
            when (owner) {
                is JccScopedExpansionUnit   -> "#${owner.name}"
                is JccNonTerminalProduction -> "${owner.name}()"
                else                        -> super.getElementText(element)
            }
        }

    override fun getContainerText(element: PsiElement?, name: String?): String? =
        when (element) {
            is JccScopedExpansionUnit   ->
                // TODO this causes reparse, should use the stubs!
                element.ancestors(includeSelf = false)
                    .filterIsInstance<JccNonTerminalProduction>()
                    .firstOrNull()
                    ?.let { "in ${it.name}()" }
            is JccNonTerminalProduction ->
                element.jjtreeNodeDescriptor.let {
                    if (it == null) ""
                    else "#${it.name}"
                }
            else                        -> ""
        }
}