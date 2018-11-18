package com.github.oowekyala.ijcc.lang.psi.light

import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.light.LightElement

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccLightIdentifier(manager: PsiManager, private var ident: String) : LightElement(manager, JavaccLanguage),
    JccIdentifier {


    override fun setName(name: String): PsiElement {
        ident = name
        return this
    }


    override fun getName(): String = ident


    override fun toString(): String = name
}