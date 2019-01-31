package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
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

    @Language("JavaCC")
    fun String.inGrammarCtx(): String = asJccGrammar().containingFile.text

    fun String.asExpansion(): JccExpansion = JccElementFactory.createBnfExpansion(getProject(), this)

    fun String.asJccFile(): JccFile = JccElementFactory.createFile(getProject(), this)

    fun String.asJccGrammar(): JccFile =
            JccElementFactory.createFile(getProject(), "${JccTestBase.DummyHeader}$this")


}