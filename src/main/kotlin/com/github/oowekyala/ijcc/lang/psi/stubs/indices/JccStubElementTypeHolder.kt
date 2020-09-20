package com.github.oowekyala.ijcc.lang.psi.stubs.indices

import com.github.oowekyala.ijcc.lang.psi.stubs.BnfProductionStubImpl
import com.github.oowekyala.ijcc.lang.psi.stubs.JavacodeProductionStubImpl
import com.github.oowekyala.ijcc.lang.psi.stubs.JccFileStub
import com.github.oowekyala.ijcc.lang.psi.stubs.JccScopedExpansionUnitStub

object JccStubElementTypeHolder {

    val bnfProd = BnfProductionStubImpl.TYPE
    val javacodeProd = JavacodeProductionStubImpl.TYPE
    val unitStub = JccScopedExpansionUnitStub.TYPE
    val fileStub = JccFileStub.TYPE
}