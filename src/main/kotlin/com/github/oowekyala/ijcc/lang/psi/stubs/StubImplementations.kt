package com.github.oowekyala.ijcc.lang.psi.stubs

import com.github.oowekyala.ijcc.CongoccLanguage
import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.model.AccessModifier
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.model.parserQualifiedName
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccBnfProductionImpl
import com.github.oowekyala.ijcc.lang.psi.impl.JccJavacodeProductionImpl
import com.github.oowekyala.ijcc.lang.psi.impl.JccScopedExpansionUnitImpl
import com.intellij.lang.Language
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IStubFileElementType

/*
    DONT FORGET TO BUMP VERSION NUMBERS WHEN CHANGING SERIALIZED STRUCTURE
 */
private const val StubVersion = 9

interface JccStub<T : JccPsiElement> : StubElement<T> {

    val fileStub: JccFileStub?
        get() = ancestors(includeSelf = true).filterIsInstance<JccFileStub>().firstOrNull()

}

class JccFileStub(val file: JccFile?,
                  /** The nature is stubbed to allow JJTree nodes to be ignored. */
                  val nature: GrammarNature,
                  val jjtreeNodeNamePrefix: String,
                  val jjtreeNodePackage: String,
                  val jccParserFileQname: String)
    : PsiFileStubImpl<JccFile>(file), JccStub<JccFile> {

    companion object {
        val TYPE: StubType by lazy { StubType("JCC_FILE", JavaccLanguage.INSTANCE) }
        val CCC_TYPE: StubType by lazy { StubType("CCC_FILE", CongoccLanguage.INSTANCE) }
    }

    class StubType(name: String, language: Language) : IStubFileElementType<JccFileStub>(name, language) {

        override fun getStubVersion(): Int = StubVersion

        override fun getBuilder(): StubBuilder = object : DefaultStubBuilder() {
            override fun createStubForFile(file: PsiFile): StubElement<*> =
                JccFileStub(
                    file = file as JccFile,
                    nature = file.grammarNature,
                    jjtreeNodeNamePrefix = file.grammarOptions.nodePrefix,
                    jjtreeNodePackage = file.grammarOptions.nodePackage,
                    jccParserFileQname = file.grammarOptions.parserQualifiedName
                )
        }

        override fun serialize(stub: JccFileStub, dataStream: StubOutputStream) {
            super.serialize(stub, dataStream)
            with(dataStream) {
                writeEnum(stub.nature)
                writeUTFFast(stub.jjtreeNodeNamePrefix)
                writeUTFFast(stub.jjtreeNodePackage)
                writeUTFFast(stub.jccParserFileQname)
            }
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JccFileStub {
            with(dataStream) {
                val nature = readEnum<GrammarNature>()
                val prefix = readUTFFast()
                val pack = readUTFFast()
                val qname = readUTFFast()
                return JccFileStub(
                    file = null,
                    nature = nature,
                    jjtreeNodeNamePrefix = prefix,
                    jjtreeNodePackage = pack,
                    jccParserFileQname = qname
                )
            }
        }
    }
}


fun factory(id: String): IElementType = when (id) {
    "JCC_BNF_PRODUCTION"        -> BnfProductionStubImpl.TYPE
    "JCC_JAVACODE_PRODUCTION"   -> JavacodeProductionStubImpl.TYPE
    "JCC_SCOPED_EXPANSION_UNIT" -> JccScopedExpansionUnitStub.TYPE
    else -> error("Unknown element $id")
}


abstract class JjtNodeClassOwnerStub<T : JjtNodeClassOwner>(parent: StubElement<*>?,
                                                            elementType: IStubElementType<out StubElement<*>, *>?,
                                                            val jjtNodeRawName: String?)
    : StubBase<T>(parent, elementType), JccStub<T> {

    val jjtNodeQualifiedName: String?
        by lazy {
            jjtNodeRawName?.let { raw ->
                fileStub?.let {
                    it.jjtreeNodePackage + "." + it.jjtreeNodeNamePrefix + raw
                }
            }
        }

    val isVoid: Boolean get() = jjtNodeRawName == null
}


abstract class NodeClassOwnerStubElementType<TStub : JjtNodeClassOwnerStub<TPsi>, TPsi : JjtNodeClassOwner>(id: String)
    : JccStubElementType<TStub, TPsi>(id) {

    /**
     * DONT FORGET TO CALL super.serialize when overriding
     */
    override fun serialize(stub: TStub, dataStream: StubOutputStream) {
        dataStream.writeNullable(stub.jjtNodeRawName) { writeUTF(it) }
    }

    final override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): TStub {
        val rawName = dataStream.readNullable { readUTF() }
        return deserializeImpl(dataStream, parentStub, rawName = rawName)
    }

    abstract fun deserializeImpl(dataStream: StubInputStream, parentStub: StubElement<*>, rawName: String?): TStub

    override fun indexStub(stub: TStub, sink: IndexSink) {
        StubIndexService.getInstance().indexJjtreeNodeClassOwner(stub, sink)
    }
}

class JccScopedExpansionUnitStub(parent: StubElement<*>?,
                                 elementType: IStubElementType<out StubElement<*>, *>?,
                                 jjtNodeRawName: String?)

    : JjtNodeClassOwnerStub<JccScopedExpansionUnit>(parent, elementType, jjtNodeRawName) {


    object TYPE :
        NodeClassOwnerStubElementType<JccScopedExpansionUnitStub, JccScopedExpansionUnit>("JCC_SCOPED_EXPANSION_UNIT") {

        override fun createPsi(stub: JccScopedExpansionUnitStub): JccScopedExpansionUnit =
            JccScopedExpansionUnitImpl(stub, this)

        override fun deserializeImpl(dataStream: StubInputStream,
                                     parentStub: StubElement<*>,
                                     rawName: String?): JccScopedExpansionUnitStub =
            JccScopedExpansionUnitStub(parentStub, this, rawName)

        override fun createStub(psi: JccScopedExpansionUnit, parentStub: StubElement<*>?): JccScopedExpansionUnitStub =
            JccScopedExpansionUnitStub(parentStub, this, psi.nodeRawName)

    }
}


abstract class NonTerminalStub<T : JccNonTerminalProduction>(parent: StubElement<*>?,
                                                             elementType: IStubElementType<out StubElement<*>, *>?,
                                                             jjtNodeRawName: String?,
                                                             val methodName: String,
                                                             val accessModifier: AccessModifier)
    : JjtNodeClassOwnerStub<T>(parent, elementType, jjtNodeRawName)


abstract class NonTerminalStubElementType<TStub : NonTerminalStub<TPsi>, TPsi : JccNonTerminalProduction>(id: String)
    : NodeClassOwnerStubElementType<TStub, TPsi>(id) {

    override fun serialize(stub: TStub, dataStream: StubOutputStream) {
        super.serialize(stub, dataStream)
        with(dataStream) {
            writeUTFFast(stub.methodName)
            writeEnum(stub.accessModifier)
        }
    }

    override fun deserializeImpl(dataStream: StubInputStream, parentStub: StubElement<*>, rawName: String?): TStub {
        val name = dataStream.readUTFFast()
        val accessMod = dataStream.readEnum<AccessModifier>()

        return deserializeImpl(dataStream, parentStub, rawName, name, accessMod)
    }

    abstract fun deserializeImpl(dataStream: StubInputStream,
                                 parentStub: StubElement<*>?,
                                 rawName: String?,
                                 methodName: String,
                                 accessModifier: AccessModifier): TStub

}


class BnfProductionStubImpl(parent: StubElement<*>?,
                            elementType: IStubElementType<out StubElement<*>, *>?,
                            methodName: String,
                            accessModifier: AccessModifier,
                            jjtNodeRawName: String?)
    : NonTerminalStub<JccBnfProduction>(parent, elementType, jjtNodeRawName, methodName, accessModifier) {


    object TYPE : NonTerminalStubElementType<BnfProductionStubImpl, JccBnfProduction>("JCC_BNF_PRODUCTION") {
        override fun createPsi(stub: BnfProductionStubImpl): JccBnfProduction =
            JccBnfProductionImpl(stub, this)

        override fun createStub(psi: JccBnfProduction, parentStub: StubElement<*>?): BnfProductionStubImpl =
            BnfProductionStubImpl(
                parentStub,
                elementType = this,
                accessModifier = psi.header.javaAccessModifier.modelConstant,
                jjtNodeRawName = psi.nodeRawName,
                methodName = psi.name
            )

        override fun deserializeImpl(dataStream: StubInputStream,
                                     parentStub: StubElement<*>?,
                                     rawName: String?,
                                     methodName: String,
                                     accessModifier: AccessModifier): BnfProductionStubImpl =
            BnfProductionStubImpl(
                parent = parentStub,
                elementType = this,
                methodName = methodName,
                jjtNodeRawName = rawName,
                accessModifier = accessModifier
            )

    }

}


class JavacodeProductionStubImpl(parent: StubElement<*>?,
                                 elementType: IStubElementType<out StubElement<*>, *>?,
                                 methodName: String,
                                 accessModifier: AccessModifier,
                                 jjtNodeRawName: String?)
    : NonTerminalStub<JccJavacodeProduction>(parent, elementType, jjtNodeRawName, methodName, accessModifier) {


    object TYPE : NonTerminalStubElementType<JavacodeProductionStubImpl, JccJavacodeProduction>("JCC_JAVACODE_PRODUCTION") {
        override fun createPsi(stub: JavacodeProductionStubImpl): JccJavacodeProduction =
            JccJavacodeProductionImpl(stub, this)

        override fun createStub(psi: JccJavacodeProduction, parentStub: StubElement<*>?): JavacodeProductionStubImpl =
            JavacodeProductionStubImpl(
                parentStub,
                elementType = this,
                accessModifier = psi.header.javaAccessModifier.modelConstant,
                jjtNodeRawName = psi.nodeRawName,
                methodName = psi.name
            )

        override fun deserializeImpl(dataStream: StubInputStream,
                                     parentStub: StubElement<*>?,
                                     rawName: String?,
                                     methodName: String,
                                     accessModifier: AccessModifier): JavacodeProductionStubImpl =
            JavacodeProductionStubImpl(
                parent = parentStub,
                elementType = this,
                methodName = methodName,
                jjtNodeRawName = rawName,
                accessModifier = accessModifier
            )
    }

}











