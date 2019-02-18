package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccScopedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.ancestors
import com.github.oowekyala.ijcc.lang.psi.stubs.JccScopedExpansionUnitStub
import com.github.oowekyala.ijcc.lang.psi.stubs.NonTerminalStub
import com.github.oowekyala.ijcc.lang.psi.stubs.ancestors
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

    // Take care of using stubs here, otherwise the file is parsed even
    // when we don't jump

    override fun getIcon(element: PsiElement?): Icon = JccIcons.JJTREE_NODE

    override fun getElementText(element: PsiElement): String =
        (element as? JjtNodeClassOwner).let { owner ->
            when (owner) {
                is JccScopedExpansionUnit   -> "#${owner.nodeRawName}"
                is JccNonTerminalProduction -> "${owner.name}()"
                else                        -> super.getElementText(element)
            }
        }

    override fun getContainerText(element: PsiElement?, name: String?): String? =
        when (element) {
            is JccScopedExpansionUnit   -> {
                val stub: JccScopedExpansionUnitStub? = element.stub

                val containerName = if (stub != null) {
                    stub.ancestors(includeSelf = false)
                        .filterIsInstance<NonTerminalStub<*>>()
                        .firstOrNull()
                        ?.methodName
                } else {
                    element.ancestors(includeSelf = false)
                        .filterIsInstance<JccNonTerminalProduction>()
                        .firstOrNull()
                        ?.name
                }

                containerName?.let { "in $it()" }
            }

            is JccNonTerminalProduction -> {
                val raw = element.nodeRawName
                val method = element.name
                if (raw != method) {
                    "#$raw"
                } else null
            }
            else                        -> null
        }
}