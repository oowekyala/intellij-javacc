package com.github.oowekyala.ijcc.lang.psi.stubs.indices

import com.github.oowekyala.ijcc.lang.psi.JccBnfProduction
import com.github.oowekyala.ijcc.lang.psi.JccJavacodeProduction
import com.github.oowekyala.ijcc.lang.psi.JccPsiElement
import com.github.oowekyala.ijcc.lang.psi.JccScopedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.stubs.BnfProductionStubImpl
import com.github.oowekyala.ijcc.lang.psi.stubs.JavacodeProductionStubImpl
import com.github.oowekyala.ijcc.lang.psi.stubs.JccFileStub
import com.github.oowekyala.ijcc.lang.psi.stubs.JccScopedExpansionUnitStub
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.StubFileElementType

interface JccStubElementTypeHolder {
        companion object {
                @JvmField
                val bnfProd: StubElementType<JccBnfProduction> = cast(BnfProductionStubImpl.TYPE)

                @JvmField
                val javacodeProd: StubElementType<JccJavacodeProduction> = cast(JavacodeProductionStubImpl.TYPE)

                @JvmField
                val unitStub: StubElementType<JccScopedExpansionUnit> = cast(JccScopedExpansionUnitStub.TYPE)

                @JvmField
                val fileStub: StubFileElementType<JccFileStub> = JccFileStub.TYPE
        }
}


/** Is needed to avoid very long type names */
private typealias StubElementType<T> = IStubElementType<StubElement<T>, T>

@Suppress("UNCHECKED_CAST")
private fun <T : JccPsiElement?> cast(type: IElementType): IStubElementType<StubElement<T>, T> {
        return type as IStubElementType<StubElement<T>, T>
}
