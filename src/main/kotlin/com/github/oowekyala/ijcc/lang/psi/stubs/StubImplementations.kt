package com.github.oowekyala.ijcc.lang.psi.stubs

import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccBnfProductionImpl
import com.github.oowekyala.ijcc.lang.psi.impl.JccJavacodeProductionImpl
import com.github.oowekyala.ijcc.lang.psi.impl.JccScopedExpansionUnitImpl
import com.github.oowekyala.ijcc.lang.psi.stubs.indices.JjtreeQNameStubIndex
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.util.io.java.AccessModifier
import readEnum
import readNullable
import writeEnum
import writeNullable


/**
 * TODO stub doc?
 */

class JccFileStub(val file: JccFile) : PsiFileStubImpl<JccFile>(file) {

    val nature: GrammarNature get() = file.grammarNature

    //    val jjtreeNodeNamePrefix: String get() = file.grammarOptions.nodePrefix
    //    val jjtreeNodePackage: String get() = file.grammarOptions.nodePackage
    //    val jccParserFileQname: String get() = file.grammarOptions.parserQualifiedName

    object Type : IStubFileElementType<JccFileStub>("JCC_FILE", JavaccLanguage) {

        //        override fun serialize(stub: JccFileStub, dataStream: StubOutputStream) {
        //            super.serialize(stub, dataStream)
        //            with(dataStream) {
        //                writeEnum(stub.nature)
        //                writeUTFFast(stub.jjtreeNodeNamePrefix)
        //                writeUTFFast(stub.jjtreeNodePackage)
        //                writeUTFFast(stub.jccParserFileQname)
        //            }
        //        }
        //
        //        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JccFileStub {
        //            with(dataStream) {
        //                val nature = readEnum<GrammarNature>()
        //                val prefix = readUTFFast()
        //                val pack = readUTFFast()
        //                val qname = readUTFFast()
        //                return
        //            }
        //        }


    }

}


fun factory(id: String): IElementType = when (id) {
    "JCC_BNF_PRODUCTION"        -> BnfProductionStubImpl.TYPE
    "JCC_JAVACODE_PRODUCTION"   -> JavacodeProductionStubImpl.TYPE
    "JCC_SCOPED_EXPANSION_UNIT" -> JccScopedExpansionUnitStub.Type
    else                        -> IJccElementType(id)
}


interface JjtNodeClassOwnerStub<T : JjtNodeClassOwner> : StubElement<T> {
    val jjtNodeQualifiedName: String?
    val isVoid: Boolean get() = jjtNodeQualifiedName == null
}


abstract class NodeClassOwnerStubElementType<TStub : JjtNodeClassOwnerStub<TPsi>, TPsi : JjtNodeClassOwner>(id: String)
    : JccStubElementType<TStub, TPsi>(id) {

    /**
     * DONT FORGET TO CALL super.serialize when overriding
     */
    override fun serialize(stub: TStub, dataStream: StubOutputStream) {
        dataStream.writeNullable(stub.jjtNodeQualifiedName) { writeUTF(it) }
    }

    final override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): TStub {
        val rawName = dataStream.readNullable { readUTF() }
        return deserializeImpl(dataStream, parentStub, nodeQname = rawName)
    }

    abstract fun deserializeImpl(dataStream: StubInputStream, parentStub: StubElement<*>, nodeQname: String?): TStub

    override fun indexStub(stub: TStub, sink: IndexSink) {
        val qname = stub.jjtNodeQualifiedName
        if (qname != null) {
            sink.occurrence(JjtreeQNameStubIndex.key, qname)
        }
    }
}

class JccScopedExpansionUnitStub(parent: StubElement<*>?,
                                 elementType: IStubElementType<out StubElement<*>, *>?,
                                 override val jjtNodeQualifiedName: String?)

    : StubBase<JccScopedExpansionUnit>(parent, elementType), JjtNodeClassOwnerStub<JccScopedExpansionUnit> {


    object Type :
        NodeClassOwnerStubElementType<JccScopedExpansionUnitStub, JccScopedExpansionUnit>("SCOPED_EXPANSION_UNIT") {

        override fun createPsi(stub: JccScopedExpansionUnitStub): JccScopedExpansionUnit =
            JccScopedExpansionUnitImpl(stub, this)

        override fun deserializeImpl(dataStream: StubInputStream,
                                     parentStub: StubElement<*>,
                                     nodeQname: String?): JccScopedExpansionUnitStub =
            JccScopedExpansionUnitStub(parentStub, this, nodeQname)

        override fun createStub(psi: JccScopedExpansionUnit, parentStub: StubElement<*>?): JccScopedExpansionUnitStub =
            JccScopedExpansionUnitStub(parentStub, this, psi.nodeQualifiedName)

    }
}


interface NonTerminalStub<T : JccNonTerminalProduction> : JjtNodeClassOwnerStub<T> {

    val methodName: String
    val accessModifier: AccessModifier
}

abstract class NonTerminalStubElementType<TStub : NonTerminalStub<TPsi>, TPsi : JccNonTerminalProduction>(id: String)
    : NodeClassOwnerStubElementType<TStub, TPsi>(id) {

    override fun serialize(stub: TStub, dataStream: StubOutputStream) {
        super.serialize(stub, dataStream)
        with(dataStream) {
            writeUTFFast(stub.methodName)
            writeEnum(stub.accessModifier)
        }
    }

    override fun deserializeImpl(dataStream: StubInputStream, parentStub: StubElement<*>, nodeQname: String?): TStub {
        val name = dataStream.readUTFFast()
        val accessMod = dataStream.readEnum<AccessModifier>()

        return deserializeImpl(dataStream, parentStub, nodeQname, name, accessMod)
    }

    abstract fun deserializeImpl(dataStream: StubInputStream,
                                 parentStub: StubElement<*>?,
                                 jjtNodeQualifiedName: String?,
                                 methodName: String,
                                 accessModifier: AccessModifier): TStub

}


class BnfProductionStubImpl(parent: StubElement<*>?,
                            elementType: IStubElementType<out StubElement<*>, *>?,
                            override val methodName: String,
                            override val accessModifier: AccessModifier,
                            override val jjtNodeQualifiedName: String?)
    : StubBase<JccBnfProduction>(parent, elementType), NonTerminalStub<JccBnfProduction> {


    object TYPE : NonTerminalStubElementType<BnfProductionStubImpl, JccBnfProduction>("BNF_PRODUCTION") {
        override fun createPsi(stub: BnfProductionStubImpl): JccBnfProduction =
            JccBnfProductionImpl(stub, this)

        override fun createStub(psi: JccBnfProduction, parentStub: StubElement<*>?): BnfProductionStubImpl =
            BnfProductionStubImpl(
                parentStub,
                elementType = this,
                accessModifier = psi.header.javaAccessModifier.modelConstant,
                jjtNodeQualifiedName = psi.nodeQualifiedName,
                methodName = psi.name
            )

        override fun deserializeImpl(dataStream: StubInputStream,
                                     parentStub: StubElement<*>?,
                                     jjtNodeQualifiedName: String?,
                                     methodName: String,
                                     accessModifier: AccessModifier): BnfProductionStubImpl =
            BnfProductionStubImpl(
                parent = parentStub,
                elementType = this,
                methodName = methodName,
                jjtNodeQualifiedName = jjtNodeQualifiedName,
                accessModifier = accessModifier
            )

    }

}


class JavacodeProductionStubImpl(parent: StubElement<*>?,
                                 elementType: IStubElementType<out StubElement<*>, *>?,
                                 override val methodName: String,
                                 override val accessModifier: AccessModifier,
                                 override val jjtNodeQualifiedName: String?)
    : StubBase<JccJavacodeProduction>(parent, elementType), NonTerminalStub<JccJavacodeProduction> {


    object TYPE : NonTerminalStubElementType<JavacodeProductionStubImpl, JccJavacodeProduction>("JAVACODE_PRODUCTION") {
        override fun createPsi(stub: JavacodeProductionStubImpl): JccJavacodeProduction =
            JccJavacodeProductionImpl(stub, this)

        override fun createStub(psi: JccJavacodeProduction, parentStub: StubElement<*>?): JavacodeProductionStubImpl =
            JavacodeProductionStubImpl(
                parentStub,
                elementType = this,
                accessModifier = psi.header.javaAccessModifier.modelConstant,
                jjtNodeQualifiedName = psi.nodeQualifiedName,
                methodName = psi.name
            )

        override fun deserializeImpl(dataStream: StubInputStream,
                                     parentStub: StubElement<*>?,
                                     jjtNodeQualifiedName: String?,
                                     methodName: String,
                                     accessModifier: AccessModifier): JavacodeProductionStubImpl =
            JavacodeProductionStubImpl(
                parent = parentStub,
                elementType = this,
                methodName = methodName,
                jjtNodeQualifiedName = jjtNodeQualifiedName,
                accessModifier = accessModifier
            )
    }

}











