// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.model.JavaccConfig
import com.intellij.psi.NavigatablePsiElement
import org.intellij.grammar.java.JavaHelper

interface JccNonTerminalProduction : JavaccPsiElement, JccIdentifierOwner, JccNodeClassOwner {

    val javaBlock: JccJavaBlock

    val header: JccJavaNonTerminalProductionHeader

    @JvmDefault
    override fun getNameIdentifier(): JccIdentifier = header.nameIdentifier

    val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor?

    @JvmDefault
    override fun nodeClass(javaccConfig: JavaccConfig): NavigatablePsiElement? {
        val nodeDescriptor = jjtreeNodeDescriptor
        if (nodeDescriptor == null && javaccConfig.isDefaultVoid || nodeDescriptor?.isVoid == true) return null

        val nodePackage = javaccConfig.nodePackage
        val nodeName = javaccConfig.nodePrefix + if (nodeDescriptor != null) nodeDescriptor.name else this.name

        return JavaHelper.getJavaHelper(this).findClass("$nodePackage.$nodeName")
    }

}
