// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.intellij.util.ThreeState

interface JccNonTerminalProduction : JccIdentifierOwner, JccNodeClassOwner {

    val javaBlock: JccJavaBlock?

    override val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor?

    val header: JccJavaNonTerminalProductionHeader

    override fun getNameIdentifier(): JccIdentifier

    /**
     * Populated by the whole-file inspection, used by the highlight visitor
     */
    val isNullable: ThreeState

}
