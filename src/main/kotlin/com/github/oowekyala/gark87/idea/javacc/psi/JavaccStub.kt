package com.github.oowekyala.gark87.idea.javacc.psi

import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCElementTypes
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.psi.stubs.StubElement

/**
 * Base class for javacc psi elements.
 *
 * @author gark87
 */
open class JavaccStub(node: ASTNode) : StubBasedPsiElementBase<StubElement<*>>(node), JavaccPsiElement {

    override fun getLanguage(): Language = JavaCCElementTypes.LANG
}
