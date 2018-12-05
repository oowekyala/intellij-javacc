package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.documentation.DocumentationMarkup.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

/**
 * Utilities to build the quickdoc.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JccDocUtil {

    private const val TerminalRef = "token"
    private const val NonterminalRef = "nonterminal"


    /** Finds the target of a link created by [getLinkRefTo]. */
    @Suppress("UNUSED_PARAMETER")
    fun findLinkTarget(psiManager: PsiManager?, ref: String?, context: PsiElement?): PsiElement? {
        val psiFile = context?.containingFile as? JccFile ?: return null
        val (type, name) = ref?.split("/") ?: return null

        return when (type) {
            TerminalRef    -> psiFile.globalNamedTokens
            NonterminalRef -> psiFile.nonTerminalProductions
            else           -> emptySequence()
        }.filter { it.name == name }.firstOrNull()
    }

    /** Gets a hyperlink suitable for use with [DocumentationManager.createHyperlink]. */
    fun getLinkRefTo(spec: JccRegexprSpec): String = "$TerminalRef/${spec.name}"

    /** Gets a hyperlink suitable for use with [DocumentationManager.createHyperlink]. */
    fun getLinkRefTo(production: JccNonTerminalProduction): String = "$NonterminalRef/${production.name}"


    fun emph(it: String) = "<i>$it</i>"
    fun bold(it: String) = "<b>$it</b>"
    fun angles(it: String) = "&lt;$it&gt;"

    /** Builds a quickdoc using a DSL, hiding most HTML formatting away. */
    fun buildQuickDoc(spec: DocBuilder.() -> Unit): String = StringBuilder().also { DocBuilder(it).spec() }.toString()

    class DocBuilder(private val stringBuilder: StringBuilder) {

        fun definition(defContents: () -> String) =
                buildDefinition {
                    append(defContents())
                }


        fun buildDefinition(defContents: java.lang.StringBuilder.() -> Unit) {
            stringBuilder.append(DEFINITION_START)
            stringBuilder.defContents()
            stringBuilder.append(DEFINITION_END).append("\n")
        }

        fun sections(sectionDefs: SectionsBuilder.() -> Unit) {
            stringBuilder.append(SECTIONS_START)
            SectionsBuilder(stringBuilder).sectionDefs()
            stringBuilder.append(SECTIONS_END)
        }

    }

    class SectionsBuilder(private val stringBuilder: StringBuilder) {

        fun emptySection(header: String) = buildSection(header, sectionDelim = "") { }

        fun section(header: String, sectionDelim: String = ":", body: () -> String) =
                buildSection(header, sectionDelim) {
                    append(body())
                }

        fun buildSection(header: String, sectionDelim: String = ":", body: java.lang.StringBuilder.() -> Unit) {
            stringBuilder.append(SECTION_HEADER_START)
            stringBuilder.append(header).append(sectionDelim)
            stringBuilder.append(SECTION_SEPARATOR).append("<p>")
            stringBuilder.body()
            stringBuilder.append(SECTION_END).append("\n")
        }
    }

}