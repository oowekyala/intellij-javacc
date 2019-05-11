package com.github.oowekyala.jjtx.jjtree

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.util.init
import com.github.oowekyala.jjtx.jjtree.JjtxCompilVisitor.OutStream.Endl
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.util.*

val bos = ByteArrayOutputStream()

/**
 * @author Clément Fournier
 */
class JjtricksToJavacc {

    fun toJavacc(jccFile: JccFile): String {

        bos.reset()

        val visitor = JjtxCompilVisitor(jccFile, bos, VanillaJjtreeBuilder(jccFile.grammarOptions))
        jccFile.grammarFileRoot!!.accept(visitor)

        return bos.toString(Charsets.UTF_8.name())
    }
}


private fun bgen(arg: String = ""): String {
    val a = if (arg.isNotEmpty()) " ${arg.trim()} " else ""
    return "/*bgen(jjtricks)$a*/"
}

private fun egen() = "/*egen*/"

private fun JccParserDeclaration.addImports(vararg qnames: String) {

    val unit = javaCompilationUnit!!.text
    val indent = " ".repeat(unit.minCommonIndent())


    val imports = StringBuilder("\n")
    for (qname in qnames) {
        imports.append("${indent}import $qname;\n")
    }

    val idx = packageRegex.find(unit)?.range?.last ?: 0

    val jcu = project.jccEltFactory.createJcu(StringBuilder(unit).insert(idx, imports).toString())

    javaCompilationUnit!!.replace(jcu)
}


private val packageRegex = Regex("\\bpackage\\s+([.\\w]+)\\s*;")

private fun JccJavaCompilationUnit.detectIndent(): String = " ".repeat(text.minCommonIndent())
private fun JccJavaBlock.detectIndent(): String {
    val contents = text.removeSurrounding("{", "}").trim()
    val min = contents.minCommonIndent()
    return if (min > 0) " ".repeat(min) else "    "
}

private fun String.minCommonIndent(): Int =
    lines().filter(String::isNotBlank).map(String::indentWidth).min() ?: 0

private fun String.indentWidth(): Int = indexOfFirst { !it.isWhitespace() }.let { if (it == -1) length else it }

private class JjtxCompilVisitor(val file: JccFile,
                                outputStream: OutputStream,
                                val builder: JjtxBuilderStrategy) : JccVisitor() {

    private val out = OutStream(outputStream, "    ")

    private val stack = Stack<NodeVar>()


    override fun visitElement(o: PsiElement) {
        o.node.getChildren(null).forEach {
            if (it is LeafPsiElement) {
                out.printSource(it.text)
            } else {
                it.psi.accept(this)
            }
        }
    }


    override fun visitBnfProduction(o: JccBnfProduction) {
        val nodeVar = builder.makeNodeVar(o, 0) ?: return super.visitBnfProduction(o)

        with(out) {

            +o.header.text + ": " + {
                +bgen(nodeVar.nodeName) + Endl
                emitOpenNodeCode(nodeVar)
                +egen() + Endl
                +o.javaBlock!!.reindentJava(indentString).escapeJjtThis(nodeVar)
            } + Endl

            stack.push(nodeVar)
            +"" + {
                emitTryCatchUnit(o.expansion!!, nodeVar)
            } + Endl
            stack.pop()
        }

    }

    private fun JccJavaBlock.reindentJava(indent: String): String =
        text.removeSurrounding("{", "}").replaceIndent(indent).trim()

    private fun String.escapeJjtThis(nodeVar: NodeVar): String = replace("jjtThis", nodeVar.varName)


    override fun visitScopedExpansionUnit(o: JccScopedExpansionUnit) {
        val nodeVar = builder.makeNodeVar(o, stack.size) ?: return super.visitScopedExpansionUnit(o)

        with(out) {

            +bgen(nodeVar.nodeName) + Endl
            +"" + {
                emitOpenNodeCode(nodeVar)
            }
            +egen()
            stack.push(nodeVar)
            emitTryCatchUnit(o.expansionUnit, nodeVar)
            stack.pop()
        }

    }

    override fun visitJjtreeNodeDescriptor(o: JccJjtreeNodeDescriptor) {
        out.printWhiteOut(o.text)
    }

    private fun emitTryCatchUnit(expansion: JccExpansion, nodeVar: NodeVar) = out.apply {

        +bgen(nodeVar.nodeName) + Endl
        +"try " + {
            +egen() + Endl
            expansion.accept(this@JjtxCompilVisitor)
            appendln()
            +bgen() + Endl
        }
        emitTryTail(expansion.findThrown(), nodeVar)
        bgen()
    }

    private fun emitTryTail(thrown: Set<String>, nodeVar: NodeVar) = out.apply {
        if (thrown.isNotEmpty()) {
            -" catch (Throwable " + nodeVar.exceptionVar + ") " + {
                +"if (" + nodeVar.closedVar + ") " + {
                    +builder.closeNodeScope(nodeVar) + ";" + Endl
                    +nodeVar.closedVar + " = false;" + Endl
                } + " else " + {
                    +builder.popNode(nodeVar) + ";" + Endl
                }

                for (ex in thrown) {
                    +"if (" + nodeVar.exceptionVar + " instanceof " + ex + ") throw (" + ex + ") " + nodeVar.exceptionVar + ";" + Endl
                }
                +"throw (Error) " + nodeVar.exceptionVar + ";" + Endl
            }
        }
        -" finally " + {
            +"if (" + nodeVar.closedVar + ") " + {
                emitCloseNodeCode(nodeVar, true)
            } + Endl
        } + Endl
    }

    private fun emitCloseNodeCode(nodeVar: NodeVar, isFinal: Boolean) = out.apply {
        with(builder) {
            +setLastToken(nodeVar) + Endl
            +closeNodeScope(nodeVar)
            if (!isFinal) {
                +nodeVar.closedVar + " = false;" + Endl
            }

            +closeNodeHook(nodeVar) + Endl
        }
    }

    private fun emitOpenNodeCode(nodeVar: NodeVar) = out.apply {
        with(builder) {
            +nodeVar.nodeRefType + " " + nodeVar.varName + " = " + builder.createNode(nodeVar) + ";" + Endl
            +"boolean " + nodeVar.closedVar + " = true;" + Endl
            +setFirstToken(nodeVar) + Endl
            +openNodeHook(nodeVar) + Endl
            +openNodeScope(nodeVar) + Endl
        }
    }

    private class OutStream(
        outputStream: OutputStream,
        private val baseIndentString: String
    ) : PrintStream(outputStream) {

        var indentString: String = ""


        fun printSource(str: String) {
            print(str)
        }

        fun printWhiteOut(str: String) {
            val lines = str.lines()
            if (lines.size <= 1) return
            else {
                for (l in lines.init()) {
                    println()
                }
            }
        }


        operator fun String.unaryPlus(): OutStream {
            print(indentString)
            print(this)
            return this@OutStream
        }

        operator fun String.unaryMinus(): OutStream {
            print(this)
            return this@OutStream
        }

        operator fun plus(other: String): OutStream {
            print(other)
            return this@OutStream
        }

        operator fun plus(endl: Endl): OutStream {
            println()
            return this@OutStream
        }


        inline operator fun plus(e: OutStream.() -> Unit): OutStream {
            println("{")
            indentString += baseIndentString
            e()
            indentString = indentString.removeSuffix(baseIndentString)
            print(indentString)
            print("}")
            return this
        }

        object Endl

    }

}


data class NodeVar(
    val varName: String,
    val closedVar: String,
    val exceptionVar: String,
    val owner: JjtNodeClassOwner,
    /** Raw name of the node. */
    val nodeName: String,
    /** QName of the class. */
    val nodeQname: String,
    /** Type of the jjtThis variable. */
    val nodeRefType: String = nodeQname
) {
    val nodeSimpleName = nodeQname.substringAfterLast('.')
}

private fun JccExpansion.findThrown(): Set<String> =
    descendantSequence(includeSelf = true)
        .filterIsInstance<JccNonTerminalExpansionUnit>()
        .mapNotNull { it.typedReference.resolveProduction() }
        .flatMap { it.header.javaThrowsList?.javaNameList?.asSequence() ?: emptySequence() }
        .map { it.text }
        .toSet()
