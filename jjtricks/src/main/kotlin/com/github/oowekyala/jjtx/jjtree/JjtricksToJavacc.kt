package com.github.oowekyala.jjtx.jjtree

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.jjtx.jjtree.OutStream.Endl
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.*

val bos = ByteArrayOutputStream()

/**
 * @author Clément Fournier
 */
class JjtricksToJavacc {

    fun toJavacc(jccFile: JccFile): String {

        bos.reset()

        val compat = JjtreeCompat()
        val visitor = JjtxCompilVisitor(jccFile, bos, compat, VanillaJjtreeBuilder(jccFile.grammarOptions, compat))
        jccFile.grammarFileRoot!!.accept(visitor)

        return bos.toString(Charsets.UTF_8.name())
    }
}


private fun bgen(arg: String = ""): String {
    val a = if (arg.isNotEmpty()) " ${arg.trim()} " else ""
    return "/*@bgen(jjtricks)$a*/"
}

private fun egen() = "/*@egen*/ "

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

private fun String.minCommonIndent(): Int =
    lines().filter(String::isNotBlank).map(String::indentWidth).min() ?: 0

private fun String.indentWidth(): Int = indexOfFirst { !it.isWhitespace() }.let { if (it == -1) length else it }


data class JjtreeCompat(
    /**
     * Don't close the node scope before the last parser actions unit
     * in a scoped expansion unit. For example:
     *
     *     (Foo() { a } { b }) #Node
     *
     * JJTree inserts the closing code between `a` and `b`, which can
     * be confusing behaviour, since the stack isn't the same in `a`
     * and `b`.
     *
     * When set to true, this behaviour is changed and the node scope
     * is closed after `{b}`. This doesn't affect the scopes of productions,
     * since the last parser actions can be used to return `jjtThis`.
     */
    val dontCloseBeforeLastParserAction: Boolean = false,

    /**
     * If set to true, jjtThis is available in the closing condition of
     * its own node scope. In vanilla JJTree, #Node(jjtThis.something())
     * isn't compiled correctly.
     */
    val fixJjtThisConditionScope: Boolean = true,

    /**
     * If set to true, the tokens are set before calling the node open
     * and close hooks. This is better as the tokens are then available
     * inside those hooks.
     */
    val setTokensBeforeHooks: Boolean = true
)

private class JjtxCompilVisitor(val file: JccFile,
                                outputStream: OutputStream,
                                val compat: JjtreeCompat,
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
        val nodeVar = builder.makeNodeVar(o, null, 0) ?: return super.visitBnfProduction(o)

        with(out) {

            +o.header.text + ": " + {
                +bgen(nodeVar.nodeName) + Endl
                emitOpenNodeCode(nodeVar)
                +egen() + Endl
                -o.javaBlock!!.reindentJava(indentString).escapeJjtThis(nodeVar)
            } + Endl

            stack.push(nodeVar)
            +"" + {
                +bgen(nodeVar.nodeName) + Endl
                emitTryCatchUnit(o.expansion!!, nodeVar)
            } + Endl
            stack.pop()
        }

    }

    override fun visitJavacodeProduction(o: JccJavacodeProduction) {
        val nodeVar = builder.makeNodeVar(o, null, 0) ?: return super.visitJavacodeProduction(o)

        with(out) {
            emitTryCatch(nodeVar, o.thrownExceptions) {
                -o.javaBlock!!.reindentJava(indentString).escapeJjtThis(nodeVar)
            }
        }

    }

    private fun JccJavaBlock.reindentJava(indent: String): String =
        text.removeSurrounding("{", "}").replaceIndent(indent).trim()

    private fun String.escapeJjtThis(nodeVar: NodeVar): String = replace("jjtThis", nodeVar.varName)


    override fun visitScopedExpansionUnit(o: JccScopedExpansionUnit) {
        val nodeVar = builder.makeNodeVar(o, stack.lastOrNull(), stack.size) ?: return super.visitScopedExpansionUnit(o)

        with(out) {

            +bgen(nodeVar.nodeName) + Endl
            +"" + {
                emitOpenNodeCode(nodeVar)
            }
            stack.push(nodeVar)
            emitTryCatchUnit(o.expansionUnit, nodeVar)
            stack.pop()
        }

    }

    override fun visitJjtreeNodeDescriptor(o: JccJjtreeNodeDescriptor) {
        out.printWhiteOut(o.text)
    }

    override fun visitParserActionsUnit(o: JccParserActionsUnit) {

        val enclosing = stack.lastOrNull() ?: return super.visitParserActionsUnit(o)

        val endOfSequence =
            o.ancestors(includeSelf = false)
                .takeWhile { it != enclosing.owner }
                .all {
                    when {
                        it.parent is JccExpansionSequence   -> it.parent.lastChild == it
                        it is JccParenthesizedExpansionUnit -> it.occurrenceIndicator == null
                        else                                -> it !is JccOptionalExpansionUnit
                    }
                }

        if (!endOfSequence) {
            with (out) {
                -bgen() + Endl
                +"" + {
                    emitCloseNodeCode(enclosing, isFinal = false)
                }
                -egen()
            }
        }

        super.visitParserActionsUnit(o)
    }

    private fun OutStream.emitTryCatchUnit(expansion: JccExpansion, nodeVar: NodeVar) = this.apply {

        emitTryCatch(nodeVar, expansion.findThrown()) {
            expansion.accept(this@JjtxCompilVisitor)
        }
    }


    private fun OutStream.emitTryCatch(nodeVar: NodeVar, thrownExceptions: Set<String>, insides: OutStream.() -> Unit) =
        this.apply {
            +"try " + {
                +egen()
                insides()
                appendln()
                +bgen() + Endl
            }
            emitTryTail(thrownExceptions, nodeVar)
            bgen()
        }

    private fun OutStream.emitTryTail(thrown: Set<String>, nodeVar: NodeVar) = this.apply {
        if (thrown.isNotEmpty()) {
            -" catch (Throwable " + nodeVar.exceptionVar + ") " + {
                +"if (" + nodeVar.closedVar + ") " + {
                    +builder.closeNodeScope(nodeVar) + ";" + Endl
                    +nodeVar.closedVar + " = false;" + Endl
                } + " else " + {
                    +builder.popNode(nodeVar) + Endl
                }

                for (ex in thrown) {
                    +"if (" + nodeVar.exceptionVar + " instanceof " + ex + ") throw (" + ex + ") " + nodeVar.exceptionVar + ";" + Endl
                }
                +"throw (Error) " + nodeVar.exceptionVar + ";" + Endl
            }
        }
        -" finally " + {
            +"if (" + nodeVar.closedVar + ") " + {
                emitCloseNodeCode(nodeVar, true) + Endl
            } + Endl
        } + Endl
    }

    private fun OutStream.emitCloseNodeCode(nodeVar: NodeVar, isFinal: Boolean) = this.apply {
        with(builder) {

            fun doSetLastToken() =
                setLastToken(nodeVar)?.let {
                    +it + Endl
                }


            if (compat.setTokensBeforeHooks) doSetLastToken()

            +closeNodeScope(nodeVar) + Endl
            if (!isFinal) {
                +nodeVar.closedVar + " = false;" + Endl
            }

            closeNodeHook(nodeVar)?.let {
                +it + Endl
            }

            if (!compat.setTokensBeforeHooks) doSetLastToken()
        }
    }


    private fun OutStream.emitOpenNodeCode(nodeVar: NodeVar) {
        with(builder) {
            +nodeVar.nodeRefType + " " + nodeVar.varName + " = " + builder.createNode(nodeVar) + ";" + Endl
            +"boolean " + nodeVar.closedVar + " = true;" + Endl

            fun doSetFirstToken() =
                setFirstToken(nodeVar)?.let {
                    +it + Endl
                }

            if (compat.setTokensBeforeHooks) doSetFirstToken()

            openNodeHook(nodeVar)?.let {
                +it + Endl
            }
            +openNodeScope(nodeVar) + Endl

            if (!compat.setTokensBeforeHooks) doSetFirstToken()
        }
    }

}


private fun JccExpansion.findThrown(): Set<String> =
    descendantSequence(includeSelf = true)
        .filterIsInstance<JccNonTerminalExpansionUnit>()
        .mapNotNull { it.typedReference.resolveProduction() }
        .flatMap { it.thrownExceptions.asSequence() }
        .toSet()
        .plus("ParseException")
        .plus("RuntimeException")


val JccNonTerminalProduction.thrownExceptions: Set<String>
    get() = header.javaThrowsList?.javaNameList?.mapTo(mutableSetOf()) { it.text } ?: emptySet()
