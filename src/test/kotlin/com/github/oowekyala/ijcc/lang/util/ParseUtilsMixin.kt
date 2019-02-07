package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createExpansion
import com.intellij.openapi.project.Project
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
interface ParseUtilsMixin {
    fun getProject(): Project


    @Language("JavaCC")
    fun String.inExpansionCtx(): String = asExpansion().containingFile.text

    fun String.inExpansionCtx(vararg otherProdNames: String): String = inExpansionCtx().let {
        it + "\n" + otherProdNames.joinToString(separator = "\n") { "void $it():{} { \"f\" }" }
    }

    fun String.inExpansionCtx(vararg otherProdNamesAndExps: Pair<String, String>): String = inExpansionCtx().let {
        it + "\n" + otherProdNamesAndExps.joinToString(separator = "\n") { "void ${it.first}():{} { ${it.second} }" }
    }

    fun String.asExpansion(vararg otherProdNamesAndExps: Pair<String, String>): JccExpansion = inExpansionCtx().let {
        it + "\n" + otherProdNamesAndExps.joinToString(separator = "\n") { "void ${it.first}():{} { ${it.second} }" }
    }.asJccFile()
        .nonTerminalProductions
        .first()
        .let { it as JccBnfProduction }
        .expansion!!

    @Language("JavaCC")
    fun String.inGrammarCtx(): String = asJccGrammar().containingFile.text

    fun String.asExpansion(): JccExpansion = createExpansion(getProject(), this)

    fun String.asProduction(): JccProductionLike =
            asJccGrammar().grammarFileRoot!!.childrenSequence().filterIsInstance<JccProductionLike>().first()

    fun String.asJccFile(): JccFile = JccElementFactory.createFile(getProject(), this)

    fun String.asJccGrammar(): JccFile =
            JccElementFactory.createFile(getProject(), "${JccTestBase.DummyHeader}$this")

}

