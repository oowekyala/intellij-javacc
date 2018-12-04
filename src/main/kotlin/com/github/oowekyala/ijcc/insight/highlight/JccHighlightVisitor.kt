package com.github.oowekyala.ijcc.insight.highlight

import com.github.oowekyala.ijcc.insight.highlight.JavaccHighlightingColors.*
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.checkReference
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.errorInfo
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.highlightInfo
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.trimWhitespace
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.wrongReferenceInfo
import com.github.oowekyala.ijcc.insight.model.JavaccConfig
import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.filterMapAs
import com.github.oowekyala.ijcc.util.ifTrue
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.strictParents
import org.apache.commons.lang3.StringEscapeUtils

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
                textRange = trimWhitespace(expansionUnit),
                type = JJTREE_NODE_SCOPE.highlightType,
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
                textRange = rangeOf(nodeDescriptor),
                type = JJTREE_DECORATION.highlightType,
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

    override fun visitOptionSection(o: JccOptionSection) {
        myHolder += highlightInfo(o.firstChild, JAVACC_KEYWORD.highlightType)
    }

    override fun visitNonTerminalExpansionUnit(o: JccNonTerminalExpansionUnit) {
        myHolder += checkReference(o, NONTERMINAL_REFERENCE.highlightType)
    }

    override fun visitRegularExprProduction(o: JccRegularExprProduction) {
        o.lexicalStateList?.identifierList?.forEach {
            myHolder += highlightInfo(it, LEXICAL_STATE.highlightType)
        }
    }

    override fun visitRegularExpressionReference(o: JccRegularExpressionReference) {
        myHolder += checkReference(o, TOKEN_REFERENCE.highlightType)
    }

    override fun visitLiteralRegularExpression(literal: JccLiteralRegularExpression) {
        val ref: JccRegexprSpec? = literal.reference?.resolve()

        // if so, the literal declares itself
        val isSelfReferential = ref != null && literal.strictParents().any { it === ref }

        if (ref != null && !isSelfReferential) {

            val tokenName = ref.name?.let { "token <$it>" } ?: "a token"
            myHolder += highlightInfo(
                literal,
                JavaccHighlightingColors.TOKEN_LITERAL_REFERENCE.highlightType,
                message = "Matched by $tokenName"
            )
        } // else stay default
    }

    override fun visitCharacterDescriptor(descriptor: JccCharacterDescriptor) {

        fun checkCharLength(psiElement: PsiElement, unescaped: String): Boolean {
            if (unescaped.length != 1) {
                myHolder += errorInfo(psiElement, "String in character list may contain only one character.")
                return false
            }
            return true
        }


        val left: String = try {
            StringEscapeUtils.unescapeJava(descriptor.baseCharAsString)
        } catch (e: IllegalArgumentException) {
            myHolder += errorInfo(descriptor.baseCharElement, e.message)
            return
        }
        val right: String? = try {
            StringEscapeUtils.unescapeJava(descriptor.toCharAsString)
        } catch (e: IllegalArgumentException) {
            // if toCharAsString is null then unescapeJava can't throw an exception
            myHolder += errorInfo(descriptor.toCharElement!!, e.message)
            return
        }

        val checkRange =
                checkCharLength(descriptor.baseCharElement, left)
                        && right != null && checkCharLength(descriptor.toCharElement!!, right)

        if (checkRange && (left[0].toInt() > right!![0].toInt())) {

            myHolder += errorInfo(
                descriptor,
                "Right end of character range \'$right\' has a lower ordinal value than the left end of character range \'$left\'."
            )
        }
    }

    override fun visitTryCatchExpansionUnit(tryCatch: JccTryCatchExpansionUnit) {
        if (tryCatch.catchClauseList.isEmpty() && tryCatch.finallyClause == null) {
            myHolder += errorInfo(tryCatch, "Try block must contain at least one catch or finally block.")
        }
    }

    override fun visitNamedRegularExpression(element: JccNamedRegularExpression) {
        myFile
            .descendantSequence()
            .filterMapAs<JccNamedRegularExpression>()
            .filter { element !== it && it.name == element.name }
            .any()
            .ifTrue {
                // there was at least one duplicate
                myHolder += errorInfo(element, "Multiply defined lexical token name \"${element.name}\"")
            }
    }

    override fun visitRegexprSpec(element: JccRegexprSpec) {
        // highlight the name of a global named regex
        element.regularExpression
            .let { it as? JccNamedRegularExpression }
            ?.run {
                myHolder += highlightInfo(nameIdentifier, JavaccHighlightingColors.TOKEN_REFERENCE.highlightType)
            }
        element.lexicalState?.let {
            myHolder += highlightInfo(
                it,
                JavaccHighlightingColors.LEXICAL_STATE.highlightType
            )
        }
        checkValidity(element)
    }

    private fun checkValidity(spec: JccRegexprSpec) {

        fun getAsLiteral(regex: JccRegexpLike): JccLiteralRegularExpression? = when (regex) {
            is JccLiteralRegularExpression -> regex
            is JccInlineRegularExpression  -> regex.regexpElement?.let { getAsLiteral(it) }
            is JccNamedRegularExpression   -> regex.regexpElement?.let { getAsLiteral(it) }
            else                           -> null
        }

        val regex = getAsLiteral(spec.regularExpression) ?: return

        spec.containingFile.globalTokenSpecs
            .filter { it !== spec }
            .any { getAsLiteral(it.regularExpression)?.textMatches(regex) == true }
            .ifTrue {
                myHolder += errorInfo(spec, "Duplicate definition of string token ${regex.text}")
            }
    }
}