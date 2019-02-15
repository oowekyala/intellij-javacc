package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.AbstractStubIndex

fun JccFile.getJjtreeDeclsForRawName(name: String): List<JjtNodeClassOwner> =
    (this as JccFileImpl).syntaxGrammar.getJjtreeDeclsForRawName(name)

fun JccFile.getProductionByName(name: String): JccNonTerminalProduction? =
    getProductionByNameMulti(name).firstOrNull()

fun JccFile.getProductionByNameMulti(name: String): List<JccNonTerminalProduction> =
    (this as JccFileImpl).syntaxGrammar.getProductionByNameMulti(name)


