package com.github.oowekyala.ijcc.lang.psi.stubs

import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.psi.JccPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement

/**
 * @author Clément Fournier
 * @since 1.2
 */
abstract class JccStubElementType<TStub : StubElement<*>, TElem : JccPsiElement>(val id: String)
    : IStubElementType<TStub, TElem>(id, JavaccLanguage) {

    override fun getExternalId(): String = "JCC_$id"

}