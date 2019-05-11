@file:JvmName("JjtricksToJavacc")

package com.github.oowekyala.jjtx.preprocessor

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.preprocessor.OutStream.Endl
import com.github.oowekyala.jjtx.util.position
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.*


/**
 * Reports syntax errors, returns true if any are found.
 */
fun JccFile.reportSyntaxErrors(ctx: JjtxContext): Boolean {

    val visitor = object : JccVisitor() {

        var invalidSyntax = false

        override fun visitElement(element: PsiElement) {
            element.acceptChildren(this)
        }

        override fun visitErrorElement(element: PsiErrorElement) {
            invalidSyntax = true
            ctx.messageCollector.reportNonFatal(element.errorDescription, position = element.position())
        }

    }

    this.accept(visitor)

    return visitor.invalidSyntax
}


fun toJavacc(input: JccFile, out: OutputStream, options: JavaccGenOptions) {

    val visitor = JjtxCompilVisitor(input, out, options, VanillaJjtreeBuilder(input.grammarOptions, options))
    input.grammarFileRoot!!.accept(visitor)
}

fun toJavaccString(input: JccFile, options: JavaccGenOptions = JavaccGenOptions()): String {
    val bos = ByteArrayOutputStream()
    toJavacc(input, bos, options)
    return bos.toString(Charsets.UTF_8.name())
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


private class JjtxCompilVisitor(val file: JccFile,
                                outputStream: OutputStream,
                                val compat: JavaccGenOptions,
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

    override fun visitOptionBinding(o: JccOptionBinding) {
        if (o.modelOption!!.supportedNature > GrammarNature.JAVACC) {
            // erase it
            out.printWhiteOut(o.text)
        } else {
            super.visitOptionBinding(o)
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
            with(out) {
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
            +" try " + {
                +egen()
                insides()
                appendln()
                +bgen() + Endl
            }
            emitTryTail(thrownExceptions, nodeVar)
            +egen() + Endl
        }

    private fun OutStream.emitTryTail(thrown: Set<String>, nodeVar: NodeVar) = this.apply {
        if (thrown.isNotEmpty()) {
            -" catch (Throwable " + nodeVar.exceptionVar + ") " + {
                +"if (" + nodeVar.closedVar + ") " + {
                    +builder.closeNodeScope(nodeVar) + Endl
                    +nodeVar.closedVar + " = false;" + Endl
                } + " else " + {
                    +builder.popNode(nodeVar) + Endl
                } + Endl

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
