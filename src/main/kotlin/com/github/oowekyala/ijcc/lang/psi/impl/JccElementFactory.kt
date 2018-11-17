package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.light.JccLightIdentifier
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager


/**
 * @author Clément Fournier
 * @since 1.0
 */

object JccElementFactory {

    private val Project.psiManager
        get() = PsiManager.getInstance(this)


    fun createIdentifier(project: Project, name: String): JccIdentifier {
        return JccLightIdentifier(project.psiManager, name)
    }

}
