package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.ide.highlight.JavaccHighlightingColors.*
import com.github.oowekyala.ijcc.ide.highlight.JccHighlightUtil.errorInfo
import com.github.oowekyala.ijcc.ide.highlight.JccHighlightUtil.highlightInfo
import com.github.oowekyala.ijcc.ide.highlight.JccHighlightUtil.warningInfo
import com.github.oowekyala.ijcc.ide.highlight.JccHighlightUtil.wrongReferenceInfo
import com.github.oowekyala.ijcc.ide.intentions.ReplaceOptionValueIntention
import com.github.oowekyala.ijcc.ide.intentions.ReplaceSupersedingUsageWithReferenceIntentionFix
import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.cfa.isEmptyMatchPossible
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.model.JccOptionType.BaseOptionType.BOOLEAN
import com.github.oowekyala.ijcc.lang.model.JjtOption
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.tree.TokenSet

/**
 * @author Clément Fournier
 * @since 1.0
 */
open class JccHighlightVisitor : JccVisitor(), HighlightVisitor, DumbAware {

    override fun clone(): HighlightVisitor = JccHighlightVisitor()

    override fun visit(element: PsiElement) = element.accept(this)

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
        if (file !is JccFile) return false

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

    private fun prepare(holder: HighlightInfoHolder, file: JccFile) {
        myHolderImpl = holder
        myFileImpl = SmartPointerManager.createPointer(file)
    }

    override fun visitGrammarFileRoot(o: JccGrammarFileRoot) {

        val eofRegexes = myFile.globalTokenSpecs.filter { it.regularExpression is JccEofRegularExpression }.toList()

        if (eofRegexes.size > 1) {

            for (eof in eofRegexes) {

                myHolder += JccHighlightUtil.errorInfo(
                    eof,
                    "Duplicate action/state change specification for <EOF>."
                )
            }
        }


        val tokenManagerDecls = myFile.tokenManagerDecls.toList()

        if (tokenManagerDecls.size > 1) {

            tokenManagerDecls.drop(1).forEach {
                myHolder += errorInfo(
                    it,
                    "Duplicate token manager declarations, at most one occurrence expected"
                )
            }
        }
    }

    override fun visitScopedExpansionUnit(o: JccScopedExpansionUnit) {
        myHolder += highlightInfo(
            element = o.expansionUnit,
            type = JJTREE_NODE_SCOPE.highlightType,
            message = "In the node scope of #${o.jjtreeNodeDescriptor.name ?: "void"}"
        )
    }

    override fun visitParserDeclaration(o: JccParserDeclaration) {


        val pEnd = o.parserEnd?.identifier
        val pBegin = o.parserBegin.identifier


        if (pEnd != null && pBegin != null) {
            if (pEnd.name != pBegin.name) {
                myHolder += errorInfo(
                    pEnd,
                    "Name ${pEnd.name} must be the same as that used at PARSER_BEGIN (${pBegin.name})"
                )
            }

            val parserClassName = o.text.let { classRegex.find(it) }?.groups?.get(1)?.value


            if (parserClassName == null) {
                myHolder += errorInfo(o, "Parser class has not been defined between PARSER_BEGIN and PARSER_END")
            } else if (pBegin.name != parserClassName) {
                myHolder += errorInfo(
                    pBegin,
                    "Name must be the same as the parser class ($parserClassName)"
                )
            }
        }
    }

    override fun visitJjtreeNodeDescriptor(nodeDescriptor: JccJjtreeNodeDescriptor) {

        if (myFile.grammarNature == GrammarNature.JAVACC) {
            myHolder += errorInfo(
                nodeDescriptor,
                JccErrorMessages.unexpectedJjtreeConstruct()
            ).withQuickFix(fixes = *JccErrorMessages.changeNatureFixes(myFile, GrammarNature.JJTREE))
        }

        // extracts the range of the "#" + the range of the ident or "void" kword
        fun rangeOf(element: JccJjtreeNodeDescriptor): TextRange {
            val ident = element.nameIdentifier
            val poundRange = element.firstChild.textRange // "#"
            return if (ident != null) poundRange.union(ident.textRange)
            else element.node.getChildren(TokenSet.create(JccTypes.JCC_VOID_KEYWORD))
                .firstOrNull()
                ?.let { poundRange.union(it.textRange) }
                ?: poundRange
        }


        myHolder += highlightInfo(rangeOf(nodeDescriptor), JJTREE_DECORATION.highlightType)
    }

    override fun visitOptionBinding(binding: JccOptionBinding) {
        val opt = binding.modelOption
        if (opt == null) {
            myHolder += warningInfo(
                binding.namingLeaf,
                "Unknown option: ${binding.name}"
            )
            return
        } else if (!opt.supports(myFile.grammarNature) && myFile.grammarNature == GrammarNature.JAVACC) {
            myHolder += warningInfo(
                binding.namingLeaf,
                JccErrorMessages.unexpectedJjtreeOption()
            ).withQuickFix(*JccErrorMessages.changeNatureFixes(myFile, opt.supportedNature))

        } else {
            myHolder += highlightInfo(binding.namingLeaf, OPTION_NAME.highlightType)
        }

        if (!binding.matchesType(opt.expectedType)) {
            // this is a corner case, that option allows boolean values for backwards compatibility
            if (opt == JjtOption.NODE_FACTORY && binding.matchesType(BOOLEAN)) {
                binding.optionValue?.run {
                    val fix =
                        when {
                            binding.stringValue.toBoolean() -> arrayOf(ReplaceOptionValueIntention("\"*\""))
                            else                            -> arrayOf(ReplaceOptionValueIntention("\"\""))
                        }

                    myHolder += warningInfo(
                        this,
                        "NODE_FACTORY supports boolean values only for backwards compatibility"
                    )
                        .withQuickFix(*fix)
                }
            } else {
                binding.optionValue?.run {
                    myHolder += errorInfo(this, "Expected ${opt.expectedType}")
                }
                binding.optionValue?.run {
                    myHolder += errorInfo(this, "Expected ${opt.expectedType}")
                }
            }
        }
    }

    override fun visitNonTerminalProduction(o: JccNonTerminalProduction) {
        // check for duplicates
        myFile.getProductionByNameMulti(o.name).runIt { dups ->
            if (dups.count() > 1) {
                myHolder += errorInfo(
                    o.nameIdentifier,
                    "Duplicate production ${o.name}"
                )
            }
        }
    }

    override fun visitJavaNonTerminalProductionHeader(element: JccJavaNonTerminalProductionHeader) {
        myHolder += highlightInfo(element.nameIdentifier, NONTERMINAL_DECLARATION.highlightType)
    }

    override fun visitOptionSection(o: JccOptionSection) {
        myHolder += highlightInfo(o.firstChild, JAVACC_KEYWORD.highlightType)


        val dupsByName = o.optionBindingList.groupBy { it.name }.filterValues { it.size > 1 }

        for ((name, bindings) in dupsByName) {
            bindings.forEach { myHolder += errorInfo(it, "Duplicate option binding for option $name") }
        }

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

    override fun visitRegexProduction(o: JccRegexProduction) {
        o.lexicalStateList?.identifierList?.forEach {
            myHolder += highlightInfo(it, LEXICAL_STATE.highlightType)
        }

        o.lexicalStateList?.identifierList?.runIt { lexStates ->

            val byName = lexStates.groupBy { it.name }


            for ((name, idents) in byName) {

                if (idents.size > 1) {
                    for (ident in idents) {
                        myHolder += errorInfo(ident, "Duplicate lexical state name $name")
                    }
                }
            }
        }


    }


    override fun visitRegexExpansionUnit(o: JccRegexExpansionUnit) {

        o.regularExpression.runIt { r ->
            if (r is JccNamedRegularExpression && r.isPrivate) {
                myHolder += errorInfo(
                    r.nameTextRange,
                    "Private (with a #) regular expression cannot be defined within grammar productions"
                )
            }

            r.asSingleLiteral()?.let { literalUnit ->

                val ref: Token = o.referencedToken!!

                myHolder += when {
                    ref.isPrivate                    -> JccHighlightUtil.errorInfo(
                        literalUnit,
                        JccErrorMessages.stringLiteralIsPrivate(literalUnit.text)
                    )
                    ref.regexKind != RegexKind.TOKEN -> JccHighlightUtil.errorInfo(
                        literalUnit,
                        JccErrorMessages.stringLiteralIsNotToken(literalUnit.text, ref.regexKind)
                    )
                    ref.isIgnoreCase                 -> JccHighlightUtil.errorInfo(
                        literalUnit,
                        JccErrorMessages.stringLiteralMatchedbyIgnoreCaseCannotBeUsedInBnf(ref.name)
                    )

                    else                             -> {
                        // all is well
                        val message =
                            if (ref.isExplicit) "Matched by " + (ref.name?.let { "<$it>" } ?: "a token")
                            else "Implicitly declared token"

                        highlightInfo(
                            literalUnit,
                            TOKEN_LITERAL_REFERENCE.highlightType,
                            message = message
                        )
                    }
                }
            }
        }
    }

    override fun visitTokenReferenceRegexUnit(o: JccTokenReferenceRegexUnit) {
        val reffed = o.typedReference.resolveToken()
        myHolder +=
            when {
                reffed == null                                           ->
                    wrongReferenceInfo(o.nameIdentifier, JccErrorMessages.undefinedTokenName(o.name!!))
                reffed.isPrivate && !o.canReferencePrivate               -> wrongReferenceInfo(
                    o.nameIdentifier,
                    "Token name \"${o.name}\" refers to a private (with a #) regular expression"
                )
                reffed.regexKind != RegexKind.TOKEN && !reffed.isPrivate -> wrongReferenceInfo(
                    o.nameIdentifier,
                    "Token name ${o.name} refers to a non-token (${reffed.regexKind}) regular expression"
                )
                else                                                     -> highlightInfo(
                    o,
                    TOKEN_REFERENCE.highlightType
                )
            }


    }

    override fun visitCharacterListRegexUnit(o: JccCharacterListRegexUnit) {

        if (o.characterDescriptorList.isEmpty() && !o.isNegated) {
            myHolder += errorInfo(
                o,
                "Empty character set is not allowed as it will not match any character"
            )
        }

    }

    override fun visitCharacterDescriptor(descriptor: JccCharacterDescriptor) {

        fun checkCharLength(psiElement: PsiElement, unescaped: String): Boolean {
            if (unescaped.length != 1) {
                myHolder += errorInfo(psiElement, "String in character list may contain only one character")
                return false
            }
            return true
        }


        val left: String = try {
            descriptor.baseCharAsString.unescapeJavaString()
        } catch (e: IllegalArgumentException) {
            myHolder += errorInfo(descriptor.baseCharElement, e.message)
            return
        }
        val right: String? = try {
            descriptor.toCharAsString?.unescapeJavaString()
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
                "Right end of character range \'$right\' has a lower ordinal value than the left end of character range \'$left\'"
            )
        }
    }

    override fun visitTryCatchExpansionUnit(tryCatch: JccTryCatchExpansionUnit) {
        if (tryCatch.catchClauseList.isEmpty() && tryCatch.finallyClause == null) {
            myHolder += errorInfo(tryCatch, "Try block must contain at least one catch or finally block")
        }
    }

    override fun visitNamedRegularExpression(element: JccNamedRegularExpression) {
        myFile.lexicalGrammar
            .getTokenByNameMulti(element.name!!)
            .takeIf { it.size > 1 }
            ?.forEach { token ->
                myHolder += errorInfo(element, "Multiply defined lexical token name \"${token.name}\"")
            }
    }

    override fun visitRegexSpec(element: JccRegexSpec) {
        // highlight the name of a global named regex
        element.regularExpression
            .let { it as? JccNamedRegularExpression }
            ?.run {
                val range = nameTextRange
                val type = if (isPrivate) PRIVATE_REGEX_DECLARATION else TOKEN_DECLARATION
                myHolder += highlightInfo(range, type.highlightType)
            }
        element.lexicalStateTransition?.let {
            myHolder += highlightInfo(
                it,
                LEXICAL_STATE.highlightType
            )
        }
        checkValidity(element)
    }

    override fun visitParenthesizedExpansionUnit(o: JccParenthesizedExpansionUnit) {

        val occ = o.occurrenceIndicator
        if (occ != null && o.expansion?.isEmptyMatchPossible() == true) {
            myHolder += errorInfo(
                o, makeEmptyExpMessage(o)
            )
        }
    }

    override fun visitOptionalExpansionUnit(o: JccOptionalExpansionUnit) {

        if (o.expansion?.isEmptyMatchPossible() == true) {
            myHolder += errorInfo(
                o, makeEmptyExpMessage(o)
            )
        }
    }

    private fun checkValidity(spec: JccRegexSpec) {

        spec.asSingleLiteral()?.runIt { regex ->

            val myStates = spec.lexicalStatesOrEmptyForAll.toSet()

            myFile.lexicalGrammar
                .getLexicalStates(myStates)
                .asSequence()
                .mapNotNull { st ->
                    st.matchLiteral(regex, exact = true)
                        ?.takeUnless {
                            // this follows references
                            // the first occurrence is considered ok, others are
                            // reported as duplicates unless they're ignore case
                            it.asStringToken == regex
                        }
                        ?.let { Pair(st, it) }
                }
                .forEach { (state, token) ->

                    myHolder +=
                        if (!token.isIgnoreCase && spec.isIgnoreCase)
                            warningInfo(
                                spec,
                                JccErrorMessages.stringLiteralWithIgnoreCaseIsPartiallySuperceded(token)
                            ).let {
                                if (spec.name == null) it
                                else it.withQuickFix(ReplaceSupersedingUsageWithReferenceIntentionFix(token.asStringToken!!, spec.name!!))
                            }
                        else
                            errorInfo(spec, JccErrorMessages.duplicateStringToken(regex, state, token))

                }
        }


        spec.lexicalStateTransition?.runIt {
            if (myFile.lexicalGrammar.getLexicalState(it.name) == null) {
                myHolder += JccHighlightUtil.wrongReferenceInfo(it, "Lexical state \"${it.name}\" has not been defined")
            }
        }

        if (spec.isPrivate) {

            spec.lexicalStateTransition?.runIt {
                myHolder += errorInfo(
                    it,
                    "Lexical state changes are not permitted after private (#) regular expressions"
                )
            }

            spec.lexicalActions?.runIt {
                myHolder += errorInfo(
                    it,
                    "Actions are not permitted on private (#) regular expressions"
                )
            }
        }

        if (spec.regularExpression is JccEofRegularExpression) {

            if (spec.lexicalStatesOrEmptyForAll.isEmpty()) {
                myHolder += errorInfo(spec, "EOF action/state change must be specified for all states, i.e., <*>TOKEN:")
            }

            if (spec.regexKind != RegexKind.TOKEN) {
                myHolder += errorInfo(spec, "EOF action/state change can be specified only in a TOKEN specification")
            }
        }

        spec.name?.runIt { name ->

            if (myFile.lexicalGrammar.lexicalStates.map { it.name }.contains(name)) {
                myHolder += JccHighlightUtil.wrongReferenceInfo(
                    spec.nameIdentifier!!,
                    "Lexical token name \"$name\" is the same as that of a lexical state"
                )
            }
        }

    }

    companion object {
        fun makeEmptyExpMessage(exp: JccExpansionUnit) =
            "Expansion within \"${exp.foldingName()}\" can be matched by empty string"


        private val classRegex = Regex("\\bclass\\s+(\\w+)")
    }
}
