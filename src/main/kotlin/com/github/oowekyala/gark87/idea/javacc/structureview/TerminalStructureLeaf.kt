package com.github.oowekyala.gark87.idea.javacc.structureview

import com.github.oowekyala.gark87.idea.javacc.psi.RegexpSpec
import com.github.oowekyala.gark87.idea.javacc.util.JavaCCIcons
import javax.swing.Icon

/**
 * Leaf for a terminal node.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class TerminalStructureLeaf(private val regexpSpec: RegexpSpec) : JavaccLeafElement<RegexpSpec>(regexpSpec) {
    override fun getPresentableText(): String? {
        return regexpSpec.identifier?.name?.let { "< $it >" }
    }

    override fun getIcon(open: Boolean): Icon? = JavaCCIcons.TERMINAL.icon
}