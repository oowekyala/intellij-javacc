package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.stubs.JjtNodeClassOwnerStub
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class JjtNodeClassOwnerImpl<TStub : JjtNodeClassOwnerStub<*>>
    : JccStubBasedPsiElementImpl<TStub>, JjtNodeClassOwner {


    constructor(node: ASTNode) : super(node)

    constructor(stub: TStub, stubType: IStubElementType<TStub, *>) : super(stub, stubType)


    override val nodeQualifiedName: String?
        get() = stub?.jjtNodeQualifiedName ?: super.nodeQualifiedName

    override val nodeSimpleName: String?
        get() = stub?.jjtNodeQualifiedName?.let {
            it.takeLastWhile { it != '.' }
        } ?: super.nodeSimpleName


}