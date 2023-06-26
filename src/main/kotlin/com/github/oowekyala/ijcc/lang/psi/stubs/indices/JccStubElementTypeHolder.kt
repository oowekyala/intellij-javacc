package com.github.oowekyala.ijcc.lang.psi.stubs.indices

import com.github.oowekyala.ijcc.lang.psi.stubs.BnfProductionStubImpl
import com.github.oowekyala.ijcc.lang.psi.stubs.JavacodeProductionStubImpl
import com.github.oowekyala.ijcc.lang.psi.stubs.JccFileStub
import com.github.oowekyala.ijcc.lang.psi.stubs.JccScopedExpansionUnitStub

interface JccStubElementTypeHolder {
    companion object {
        @JvmStatic
        val bnfProd = BnfProductionStubImpl.TYPE

        @JvmStatic
        val javacodeProd = JavacodeProductionStubImpl.TYPE

        @JvmStatic
        val unitStub = JccScopedExpansionUnitStub.TYPE

        @JvmStatic
        val fileStub = JccFileStub.TYPE
    }
}
