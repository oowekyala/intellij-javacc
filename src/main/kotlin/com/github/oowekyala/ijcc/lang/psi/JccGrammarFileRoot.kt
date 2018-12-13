// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree

interface JccGrammarFileRoot : JavaccPsiElement {

    val nonTerminalProductionList: List<JccNonTerminalProduction>

    val optionSection: JccOptionSection?

    val parserDeclaration: JccParserDeclaration

    val regularExprProductionList: List<JccRegularExprProduction>

    val tokenManagerDeclsList: List<JccTokenManagerDecls>

    val injectionStructureTree: InjectionStructureTree

}
