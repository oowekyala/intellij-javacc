package com.github.oowekyala.ijcc.structure

import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.util.JavaccIcons
import javax.swing.Icon

/**
 * Leaf for a terminal node.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class TerminalStructureLeaf(private val regexpSpec: JccRegexprSpec) : JavaccLeafElement<JccRegexprSpec>(regexpSpec) {
    override fun getPresentableText(): String? =
        (regexpSpec.regularExpression as? JccNamedRegularExpression)?.let { "< ${it.name} >" }

    override fun getIcon(open: Boolean): Icon? = JavaccIcons.TERMINAL
}