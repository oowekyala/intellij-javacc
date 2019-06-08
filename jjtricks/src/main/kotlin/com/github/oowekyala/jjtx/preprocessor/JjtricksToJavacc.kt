@file:JvmName("JjtricksToJavacc")

package com.github.oowekyala.jjtx.preprocessor

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.jjtx.util.io.DslPrintStream
import com.github.oowekyala.jjtx.util.io.DslPrintStream.Endl
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.*


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
    return "/*@bgen(jjtree)$a*/" // FIXME "jjtree" is hardcoded in JavaCC's codebase
}

private fun egen() = "/*@egen*/ "

private fun JccJavaCompilationUnit.guessIndent():String {

    // The indent preceding the first non-whitespace of the JCU
    // is in the previous sibling
    val prevIndent =
        prevSibling
            ?.takeIf { it.isWhitespace }
            ?.text
            ?.substringAfterLast("\n", "")
            ?: ""

    return " ".repeat((prevIndent + this.text).minCommonIndent())

}

private fun JccJavaCompilationUnit.addImports(qnames: List<String>): String {
    val unit= this.text!!
    val indent = guessIndent()


    val imports = StringBuilder("\n")
    for (qname in qnames) {
        imports.append(indent).append("import ").append(qname).appendln(";")
    }

    val idx = (packageRegex.find(unit)?.range?.last ?: 0) + 1

    return StringBuilder(unit).insert(idx, imports).toString()
}


private val packageRegex = Regex("\\bpackage\\s+([.\\w]+)\\s*;")

private fun String.minCommonIndent(): Int =
    lines().filter(String::isNotBlank).map(String::indentWidth).min() ?: 0

private fun String.indentWidth(): Int = indexOfFirst { !it.isWhitespace() }.let { if (it == -1) length else it }


private class JjtxCompilVisitor(val file: JccFile,
                                outputStream: OutputStream,
                                val compat: JavaccGenOptions,
                                val builder: JjtxBuilderStrategy) : JccVisitor() {

    private val out = DslPrintStream.forJavaccOutput(outputStream)

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
            // comment it
            out.print("// ")
        }

        super.visitOptionBinding(o)
    }

    private val implementsRegex = Regex("implements|\\{")
    private val braceRegex = Regex("\\{")

    // So ugly
    override fun visitJavaCompilationUnit(o: JccJavaCompilationUnit) {

        val sb = StringBuilder(o.addImports(builder.parserImports()))

        if (builder.parserImplements().isNotEmpty()) {

            val implPoint = implementsRegex.find(sb)!!

            when {
                implPoint.value == "implements" -> {
                    sb.insert(
                        implPoint.range.endInclusive + 1,
                        bgen() + builder.parserImplements().joinToString(postfix = ", ") + egen()
                    )
                }
                else                            -> {
                    sb.insert(
                        implPoint.range.start - 1,
                        bgen() + builder.parserImplements().joinToString(prefix = "implements ") + egen()
                    )
                }
            }
        }

        val bracePoint = braceRegex.find(sb)!!

        val indent = o.guessIndent()

        sb.insert(bracePoint.range.endInclusive + 1, bgen() + "\n" + builder.parserDeclarations().replaceIndent(indent) + egen())

        out.printSource(sb.toString())
    }

    private fun <T> Stack<T>.top(): T? = if (isEmpty()) null else peek()

    override fun visitBnfProduction(o: JccBnfProduction) {
        val nodeVar = builder.makeNodeVar(o, stack.top()) ?: return super.visitBnfProduction(o)

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
        val nodeVar = builder.makeNodeVar(o, stack.top()) ?: return super.visitJavacodeProduction(o)

        with(out) {
            emitTryCatch(nodeVar, o.thrownExceptions) {
                -o.javaBlock!!.reindentJava(indentString).escapeJjtThis(nodeVar)
            }
        }

    }

    private fun JccJavaBlock.reindentJava(indent: String): String =
        text.removeSurrounding("{", "}").replaceIndent(indent).trim()

    private fun String.escapeJjtThis(nodeVar: NodeVar): String = builder.escapeJjtThis(nodeVar, this)


    override fun visitScopedExpansionUnit(o: JccScopedExpansionUnit) {
        val nodeVar = builder.makeNodeVar(o, stack.top()) ?: return super.visitScopedExpansionUnit(o)

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

    private fun DslPrintStream.emitTryCatchUnit(expansion: JccExpansion, nodeVar: NodeVar) = this.apply {

        emitTryCatch(nodeVar, expansion.findThrown()) {
            expansion.accept(this@JjtxCompilVisitor)
        }
    }


    private fun DslPrintStream.emitTryCatch(nodeVar: NodeVar, thrownExceptions: Set<String>, insides: DslPrintStream.() -> Unit) =
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

    private fun DslPrintStream.emitTryTail(thrown: Set<String>, nodeVar: NodeVar) = this.apply {
        if (thrown.isNotEmpty()) {
            -" catch (Throwable " + nodeVar.exceptionVar + ") " + {
                +"if (" + nodeVar.closedVar + ") " + {
                    +builder.closeNodeScope(nodeVar) + Endl
                    +nodeVar.closedVar + " = false;" + Endl
                } + " else " + {
                    +builder.popNode(nodeVar) + Endl
                } + Endl

                if (compat.castExceptions) {
                    +"// This chain of casts is meant to force you to declare" + Endl
                    +"// checked exceptions explicitly on the productions, else it fails" + Endl
                    +"// with a ClassCastException on the Error branch" + Endl
                    for (ex in thrown) {
                        +"if (" + nodeVar.exceptionVar + " instanceof " + ex + ") throw (" + ex + ") " + nodeVar.exceptionVar + ";" + Endl
                    }
                    +"throw (Error) " + nodeVar.exceptionVar + ";" + Endl
                } else {
                    +"throw " + nodeVar.exceptionVar + ";" + Endl
                }
            }
        }
        -" finally " + {
            +"if (" + nodeVar.closedVar + ") " + {
                emitCloseNodeCode(nodeVar, true)
            } + Endl
        } + Endl
    }

    private fun DslPrintStream.emitCloseNodeCode(nodeVar: NodeVar, isFinal: Boolean) = this.apply {
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


    private fun DslPrintStream.emitOpenNodeCode(nodeVar: NodeVar) {
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
        .plus(listOf("ParseException", "RuntimeException"))


val JccNonTerminalProduction.thrownExceptions: Set<String>
    get() = header.javaThrowsList?.javaNameList?.mapTo(mutableSetOf()) { it.text } ?: emptySet()
