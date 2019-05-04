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

    val inlineBindings: InlineGrammarOptions
}


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
