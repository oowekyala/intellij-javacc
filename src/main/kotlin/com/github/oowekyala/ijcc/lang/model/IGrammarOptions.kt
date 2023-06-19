package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding

/**
 * Most general options interface.
 * Handles JJTricks option files.
 */
interface IGrammarOptions {
    val nodePackage: String
    val isDefaultVoid: Boolean
    val nodePrefix: String
    val isTrackTokens: Boolean
    val nodeTakesParserArg: Boolean
    val isUserTokenManager: Boolean

    /**
     * If unknown, defaults to the empty string
     */
    val grammarName: String?

    /**
     * Fallback on the options specified in the grammar file.
     */
    val inlineBindings: InlineGrammarOptions
}

fun IGrammarOptions.addNodePackage(simpleName: String) =
    nodePackage.addPackage(simpleName)

fun IGrammarOptions.addParserPackage(simpleName: String) =
    parserPackage.addPackage(simpleName)

private fun String.addPackage(toQualify:String)=
    if (isEmpty()) toQualify else "$this.$toQualify"


val JccFile.allOptionsBindings: List<JccOptionBinding> get() = options?.optionBindingList ?: emptyList()


val IGrammarOptions.parserQualifiedName: String
    get() {
        val pack = parserPackage
        return if (pack.isEmpty()) parserSimpleName
        else "$pack.$parserSimpleName"
    }


val IGrammarOptions.parserSimpleName: String
    get() =
        inlineBindings.file.parserDeclaration?.text?.let { classRegex.find(it) }?.groups?.get(1)?.value ?: ""

private val packageRegex = Regex("\\bpackage\\s+([.\\w]+)")
private val classRegex = Regex("\\bclass\\s+(\\w+)")

val IGrammarOptions.parserPackage: String
    get() = inlineBindings.file.parserDeclaration?.text?.let { packageRegex.find(it) }?.groups?.get(1)?.value ?: ""
