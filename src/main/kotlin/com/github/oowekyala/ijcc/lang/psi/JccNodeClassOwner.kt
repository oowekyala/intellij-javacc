package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.model.JavaccConfig
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope

/**
 * Node that is tied to a concrete generated node class.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccNodeClassOwner : JavaccPsiElement {

    fun nodeClass(javaccConfig: JavaccConfig): NavigatablePsiElement?


    companion object {

        fun getJavaClassFromQname(context: JccNodeClassOwner, fqcn: String): PsiClass? {

            val scope = ModuleUtil.findModuleForPsiElement(context)
                ?.let { GlobalSearchScope.moduleScope(it) }
                ?: GlobalSearchScope.allScope(context.project)

            return JavaPsiFacade.getInstance(context.project).findClass(fqcn, scope)
        }

    }
}