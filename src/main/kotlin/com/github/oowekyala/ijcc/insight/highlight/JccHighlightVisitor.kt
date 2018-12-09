package com.github.oowekyala.ijcc.insight.highlight

import com.github.oowekyala.ijcc.insight.highlight.JavaccHighlightingColors.*
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.errorInfo
import com.github.oowekyala.ijcc.insight.highlight.JccHighlightUtil.highlightInfo
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
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.strictParents
import org.apache.commons.lang3.StringEscapeUtils

/**
 * @author Clément Fournier
 * @since 1.0
 */
open class JccHighlightVisitor : JccVisitor(), HighlightVisitor {

    override fun clone(): HighlightVisitor = JccHighlightVisitor()

    override fun visit(element: PsiElement) = element.accept(this)

    override fun order(): Int = 0

    private var myHolderImpl: HighlightInfoHolder? = null
    private val myHolder: HighlightInfoHolder // never null during analysis
        get() = myHolderImpl!!

    private var myFileImpl: SmartPsiElementPointer<JccFile>? = null
    private val myFile: JccFile // never null during analysis
        get() = myFileImpl!!.element!!

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

    override fun suitableForFile(file: PsiFile): Boolean = file is JccFile

    private fun prepare(holder: HighlightInfoHolder, file: PsiFile) {
        myHolderImpl = holder
        myFileImpl = SmartPointerManager.createPointer(file as JccFile)
    }

    override fun visitScopedExpansionUnit(o: JccScopedExpansionUnit) {
        myHolder += highlightInfo(
            element = o.expansionUnit,
            type = JJTREE_NODE_SCOPE.highlightType,
            message = "In the node scope of #${o.jjtreeNodeDescriptor.name ?: "void"}"
        )
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


        myHolder += highlightInfo(rangeOf(nodeDescriptor), JJTREE_DECORATION.highlightType)
    }

    override fun visitOptionBinding(binding: JccOptionBinding) {
        val opt = JavaccConfig.knownOptions[binding.name]
        if (opt == null) {
            myHolder += wrongReferenceInfo(
                binding.nameIdentifier!!,
                "Unknown option: ${binding.name}"
            )
            return
        } else {
            myHolder += highlightInfo(binding.namingLeaf, OPTION_NAME.highlightType)
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
        myHolder += if (o.typedReference.resolve() == null) {
            wrongReferenceInfo(
                o.nameIdentifier,
                "Non-terminal ${o.name} has not been defined"
            )
        } else {
            highlightInfo(o.nameIdentifier, NONTERMINAL_REFERENCE.highlightType)
        }
    }

    override fun visitRegularExprProduction(o: JccRegularExprProduction) {
        o.lexicalStateList?.identifierList?.forEach {
            myHolder += highlightInfo(it, LEXICAL_STATE.highlightType)
        }
    }

    override fun visitTokenReferenceUnit(o: JccTokenReferenceUnit) {
        val reffed = o.typedReference.resolveToken()
        myHolder +=
                if (reffed == null) wrongReferenceInfo(o.nameIdentifier, "Undefined lexical token name \"${o.name}\"")
                else if (reffed.isPrivate && !o.isInRegexContext) {
                    errorInfo(
                        o.nameIdentifier,
                        "Token name \"${o.name}\" refers to a private (with a #) regular expression"
                    )
                } else highlightInfo(o, TOKEN_REFERENCE.highlightType)

    }

    override fun visitLiteralRegexpUnit(o: JccLiteralRegexpUnit) {
        val ref: JccRegexprSpec? = o.typedReference?.resolve()

        // if so, the literal declares itself
        val isSelfReferential = ref != null && o.strictParents().any { it === ref }

        if (ref != null && !isSelfReferential) {

            val tokenName = ref.name?.let { "token <$it>" } ?: "a token"
            myHolder += highlightInfo(
                o,
                TOKEN_LITERAL_REFERENCE.highlightType,
                message = "Matched by $tokenName"
            )
        } // else stay default}
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
                val (range, type) = if (isPrivate) {
                    val r = this.node.findChildByType(JavaccTypes.JCC_POUND)
                        ?.textRange
                        ?.let { it.union(nameIdentifier.textRange) }
                        ?: nameIdentifier.textRange!!
                    Pair(r, PRIVATE_REGEX_DECLARATION)
                } else {
                    Pair(nameIdentifier.textRange, TOKEN_DECLARATION)
                }

                myHolder += highlightInfo(range, type.highlightType)
            }
        element.lexicalState?.let {
            myHolder += highlightInfo(
                it,
                LEXICAL_STATE.highlightType
            )
        }
        checkValidity(element)
    }

    private fun checkValidity(spec: JccRegexprSpec) {

        fun getAsLiteral(regex: JccRegexpLike): JccLiteralRegexpUnit? = when (regex) {
            is JccLiteralRegexpUnit       -> regex
            is JccInlineRegularExpression -> regex.regexpElement?.let { getAsLiteral(it) }
            is JccNamedRegularExpression  -> regex.regexpElement?.let { getAsLiteral(it) }
            else                          -> null
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