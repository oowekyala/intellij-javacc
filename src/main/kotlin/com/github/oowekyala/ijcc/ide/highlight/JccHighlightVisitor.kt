package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.ide.highlight.JavaccHighlightingColors.*
import com.github.oowekyala.ijcc.ide.highlight.JccHighlightUtil.errorInfo
import com.github.oowekyala.ijcc.ide.highlight.JccHighlightUtil.highlightInfo
import com.github.oowekyala.ijcc.ide.highlight.JccHighlightUtil.wrongReferenceInfo
import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.model.GrammarOptions
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.ifTrue
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
import com.intellij.util.containers.MostlySingularMultiMap
import gnu.trove.THashMap
import org.apache.commons.lang3.StringEscapeUtils

/**
 * @author Clément Fournier
 * @since 1.0
 */
open class JccHighlightVisitor : JccVisitor(), HighlightVisitor, DumbAware {

    override fun clone(): HighlightVisitor = JccHighlightVisitor()

    override fun visit(element: PsiElement) = element.accept(this)

    override fun order(): Int = 0

    private var myHolderImpl: HighlightInfoHolder? = null
    private val myHolder: HighlightInfoHolder // never null during analysis
        get() = myHolderImpl!!

    private var myFileImpl: SmartPsiElementPointer<JccFile>? = null
    private val myFile: JccFile // never null during analysis
        get() = myFileImpl!!.element!!


    private val myDuplicateMethods =
            THashMap<JccFile, MostlySingularMultiMap<String, JccNonTerminalProduction>>().withDefault { file ->
                // get the duplicate prods for the file
                val signatures = MostlySingularMultiMap<String, JccNonTerminalProduction>()

                for (prod in file.nonTerminalProductions) {
                    signatures.add(prod.name, prod)
                }

                signatures
            }

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
            myDuplicateMethods.clear()
        }
        return true
    }

    override fun suitableForFile(file: PsiFile): Boolean = file is JccFile

    private fun prepare(holder: HighlightInfoHolder, file: PsiFile) {
        myHolderImpl = holder
        myFileImpl = SmartPointerManager.createPointer(file as JccFile)
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
                    "Duplicate token manager declarations, at most one occurrence expected."
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
        val opt = GrammarOptions.knownOptions[binding.name]
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

    override fun visitNonTerminalProduction(o: JccNonTerminalProduction) {
        // check for duplicates
        myDuplicateMethods.getValue(o.containingFile)[o.name].runIt { dups ->
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

        o.regularExpression.runIt {
            if (it is JccNamedRegularExpression && it.isPrivate) {
                myHolder += errorInfo(
                    it.nameTextRange,
                    "Private (with a #) regular expression cannot be defined within grammar productions"
                )
            }

            it.asSingleLiteral()?.let { literalUnit ->

                val ref: Token = o.referencedToken!!

                myHolder += when {
                    ref.isPrivate    -> JccHighlightUtil.errorInfo(
                        literalUnit,
                        "String token \"${literalUnit.match}\" has been defined as a private (#) regular expression"
                    )

                    ref.isIgnoreCase -> {
                        // then it cannot be implicit

                        val tokenName = ref.name?.let { "(<$it>)" } ?: "(unnamed!)"

                        JccHighlightUtil.errorInfo(
                            literalUnit,
                            "String is matched by an IGNORE_CASE regular expression and should refer to the token by name $tokenName"
                        )
                    }
                    else             -> {
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
                if (reffed == null) wrongReferenceInfo(o.nameIdentifier, "Undefined lexical token name \"${o.name}\"")
                else if (reffed.isPrivate && !o.canReferencePrivate) {
                    wrongReferenceInfo(
                        o.nameIdentifier,
                        "Token name \"${o.name}\" refers to a private (with a #) regular expression"
                    )
                } else if (reffed.regexKind != RegexKind.TOKEN) {
                    wrongReferenceInfo(
                        o.nameIdentifier,
                        "Token name ${o.name} refers to a non-token (SKIP, MORE, IGNORE_IN_BNF) regular expression"
                    )
                } else highlightInfo(o, TOKEN_REFERENCE.highlightType)


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
        myFile
            .descendantSequence()
            .filterIsInstance<JccNamedRegularExpression>()
            .filter { element !== it && it.name == element.name }
            .any()
            .ifTrue {
                // there was at least one duplicate
                myHolder += errorInfo(element, "Multiply defined lexical token name \"${element.name}\"")
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
            spec.containingFile.globalTokenSpecs
                // only consider those above
                .filter { it.textOffset < spec.textOffset }
                .find { it.definedToken.matchesLiteral(regex) }
                ?.runIt {
                    val message = it.name?.let { " (see <$it>)" } ?: ""
                    myHolder += errorInfo(spec, "Duplicate definition of string token ${regex.text}$message")
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

            if (spec.lexicalStatesNameOrEmptyForAll.isEmpty()) {
                myHolder += errorInfo(spec, "EOF action/state change must be specified for all states, i.e., <*>TOKEN:")
            }

            if (spec.regexKind != RegexKind.TOKEN) {
                myHolder += errorInfo(spec, "EOF action/state change can be specified only in a TOKEN specification")
            }
        }

        spec.name?.runIt { name ->

            if (myFile.lexicalGrammar.lexicalStates.map { it.name }.contains(name)) {
                val id = spec.regularExpression.let { it as JccNamedRegularExpression }.nameIdentifier
                myHolder += JccHighlightUtil.wrongReferenceInfo(
                    id,
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