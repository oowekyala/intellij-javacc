package com.github.oowekyala.ijcc.insight.highlight

import com.github.oowekyala.ijcc.insight.highlight.JavaccHighlightingColors.*
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.errorInfo
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.highlightInfo
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.trimWhitespace
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.wrongReferenceInfo
import com.github.oowekyala.ijcc.insight.model.JavaccConfig
import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.TokenSet

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccHighlightVisitor : JccVisitor(), HighlightVisitor {

    override fun suitableForFile(file: PsiFile): Boolean = file is JccFile

    override fun clone(): HighlightVisitor = JccHighlightVisitor()

    override fun visit(element: PsiElement) = element.accept(this)

    override fun order(): Int = 0

    private var myHolderImpl: HighlightInfoHolder? = null
    private val myHolder: HighlightInfoHolder // never null during analysis
        get() = myHolderImpl!!

    private var myFileImpl: JccFile? = null
    private val myFile: JccFile // never null during analysis
        get() = myFileImpl!!

    override fun analyze(file: PsiFile,
                         updateWholeFile: Boolean,
                         holder: HighlightInfoHolder,
                         highlight: Runnable): Boolean {
        try {
            prepare(holder, file)
            highlight.run()
        } finally {
            // cleanup
            myFileImpl = null
            myHolderImpl = null
        }
        return true
    }

    fun prepareToRunAsInspection(holder: HighlightInfoHolder) {
        prepare(holder, holder.contextFile)
    }

    private fun prepare(holder: HighlightInfoHolder, file: PsiFile) {
        myHolderImpl = holder
        myFileImpl = file as JccFile
    }

    override fun visitJjtreeNodeDescriptor(nodeDescriptor: JccJjtreeNodeDescriptor) {
        // extracts the range of the "#" + the range of the ident or "void" kword
        fun rangeOf(element: JccJjtreeNodeDescriptor): TextRange {
            val ident = element.nameIdentifier
            val poundRange = element.firstChild.textRange // "#"
            return if (ident != null) poundRange.union(ident.textRange)
            else element.node.getChildren(TokenSet.create(JavaccTypes.JCC_VOID_KEYWORD))
                .firstOrNull()
                ?.let { poundRange.union(it.textRange) }
                ?: poundRange
        }

        val header = nodeDescriptor.productionHeader
        val expansionUnit = nodeDescriptor.expansionUnit

        if (header == null && expansionUnit != null) {
            myHolder += highlightInfo(
                trimWhitespace(expansionUnit),
                JJTREE_NODE_SCOPE.highlightType,
                message = "In the node scope of #${nodeDescriptor.name ?: "void"}"
            )

            myHolder += highlightInfo(rangeOf(nodeDescriptor), JJTREE_DECORATION.highlightType)
            //            TODO should be its own inspection
            //            if (nodeDescriptor.isVoid) {
            //                myHolder += createWeakWarningAnnotation(getRangeFor(nodeDescriptor), "Useless #void annotation")
            //            }
        } else if (header != null && expansionUnit == null) {
            // TODO move that message to quickdoc
            val message =
                    if (nodeDescriptor.isVoid) "Discards the node created by this production"
                    else "Renames this production's node to ${nodeDescriptor.name}"

            myHolder += highlightInfo(
                rangeOf(nodeDescriptor),
                JJTREE_DECORATION.highlightType,
                message = message
            )
        }
    }

    override fun visitOptionBinding(binding: JccOptionBinding) {
        val opt = JavaccConfig.knownOptions[binding.name]
        if (opt == null) {
            myHolder += wrongReferenceInfo(
                binding.nameIdentifier!!, // may not be supported for some elements (eg JjtNodeDescriptor)
                "Unknown option: ${binding.name}"
            )
            return
        }

        if (!binding.matchesType(opt.expectedType)) {
            binding.optionValue?.run {
                myHolder += errorInfo(this, "Expected ${opt.expectedType}")
            }
        }
    }

    override fun visitJavaNonTerminalProductionHeader(element: JccJavaNonTerminalProductionHeader) {
        myHolder += highlightInfo(element.nameIdentifier, NONTERMINAL_DECLARATION.highlightType)
    }


}