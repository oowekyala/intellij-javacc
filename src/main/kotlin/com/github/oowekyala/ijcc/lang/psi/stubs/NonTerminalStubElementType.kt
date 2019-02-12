package com.github.oowekyala.ijcc.lang.psi.stubs

import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.psi.JccBnfProduction
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.impl.JccBnfProductionImpl
import com.github.oowekyala.ijcc.lang.psi.impl.JccJavacodeProductionImpl
import com.github.oowekyala.ijcc.lang.psi.modelConstant
import com.github.oowekyala.ijcc.lang.psi.nodeQualifiedName
import com.github.oowekyala.ijcc.lang.psi.stubs.indices.JjtreeQNameStubIndex
import com.intellij.psi.stubs.*
import com.intellij.util.io.DataInputOutputUtil
import com.intellij.util.io.java.AccessModifier


/**
 * @author Clément Fournier
 * @since 1.2
 */
sealed class NonTerminalStubElementType(private val id: String)
    : IStubElementType<NonTerminalStub, JccNonTerminalProduction>("NON_TERMINAL", JavaccLanguage) {
    
    override fun getExternalId(): String = id

    override fun createPsi(stub: NonTerminalStub): JccNonTerminalProduction {
        return when {
            stub.isBnf -> JccBnfProductionImpl(stub, this)
            else       -> JccJavacodeProductionImpl(stub, this)
        }
    }

    override fun createStub(psi: JccNonTerminalProduction, parentStub: StubElement<*>?): NonTerminalStub =
        NonTerminalStubImpl(
            parentStub,
            elementType = this,
            accessModifier = psi.header.javaAccessModifier.modelConstant,
            isBnf = psi is JccBnfProduction,
            jjtreeNodeQname = psi.nodeQualifiedName,
            methodName = psi.name
        )


    override fun serialize(stub: NonTerminalStub, dataStream: StubOutputStream) {
        with(dataStream) {
            writeUTFFast(stub.methodName)
            DataInputOutputUtil.writeNullable(this, stub.jjtreeNodeQname) {
                dataStream.writeUTFFast(it)
            }
            writeBoolean(stub.isBnf)
            writeInt(stub.accessModifier.ordinal)
        }
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): NonTerminalStub {
        val name = dataStream.readUTFFast()
        val nodeQname = DataInputOutputUtil.readNullable(dataStream) {
            dataStream.readUTFFast()
        }
        val isBnf = dataStream.readBoolean()
        val accessMod = AccessModifier.values()[dataStream.readInt()]

        return NonTerminalStubImpl(
            parent = parentStub,
            elementType = this,
            methodName = name,
            jjtreeNodeQname = nodeQname,
            isBnf = isBnf,
            accessModifier = accessMod
        )
    }


    override fun indexStub(stub: NonTerminalStub, sink: IndexSink) {
        val qname = stub.jjtreeNodeQname
        if (qname != null) {
            sink.occurrence(JjtreeQNameStubIndex.key, qname)
        }
    }
}

object BnfStubElementType : NonTerminalStubElementType("ijcc.nonterminal.bnf")
object JavacodeStubElementType : NonTerminalStubElementType("ijcc.nonterminal.javacode")