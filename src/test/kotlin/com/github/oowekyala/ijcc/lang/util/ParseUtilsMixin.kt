package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.openapi.project.Project

/**
 * @author Clément Fournier
 * @since 1.0
 */
interface ParseUtilsMixin {
    fun getProject(): Project

    fun String.asExpansion(): JccExpansion = JccElementFactory.createBnfExpansion(getProject(), this)

    fun String.asJccFile(): JccFile = JccElementFactory.createFile(getProject(), this)


    fun String.asJccGrammar(): JccFile =
            JccElementFactory.createFile(getProject(), "${JccTestBase.DummyHeader}$this")


}