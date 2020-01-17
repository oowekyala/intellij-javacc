package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.nodeIdentifier
import com.github.oowekyala.ijcc.lang.psi.stubs.JjtNodeClassOwnerStub
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class JjtNodeClassOwnerImpl<TStub : JjtNodeClassOwnerStub<*>>
    : JccStubBasedPsiElementImpl<TStub>, JjtNodeClassOwner {


    constructor(node: ASTNode) : super(node)

    constructor(stub: TStub, stubType: IStubElementType<TStub, *>) : super(stub, stubType)


    override val nodeQualifiedName: String?
        get() = stub?.jjtNodeQualifiedName ?: nodeSimpleName?.let {
            val packageName = grammarOptions.nodePackage

            if (packageName.isEmpty()) nodeSimpleName
            else "$packageName.$it"
        }

    override val nodeSimpleName: String?
        get() = stub?.jjtNodeQualifiedName?.substringAfterLast('.')
            ?: nodeRawName?.let { grammarOptions.nodePrefix + it }

    override val nodeRawName: String?
        get() = stub?.jjtNodeRawName ?: nodeIdentifier?.name?.takeIf {
            // nothing generates nodes in jj files
            containingFile.grammarNature >= GrammarNature.JJTREE
        }

}
