package com.github.oowekyala.ijcc.lang.psi

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
interface JccNodeClassOwner : JavaccPsiElement, JccIdentifierOwner {


    val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor?

    @JvmDefault
    val nodeClass: NavigatablePsiElement?
        get() = nodeQualifiedName?.let {
            getJavaClassFromQname(containingFile, it)
        }

    @JvmDefault
    val nodeQualifiedName: String?
        get() = nodeSimpleName?.let { "${javaccConfig.nodePackage}.$it" }

    @JvmDefault
    val nodeSimpleName: String?
        get() {
            val nodeDesc = jjtreeNodeDescriptor
            val isDefaultVoid = javaccConfig.isDefaultVoid

            val base: String? =
                    if (nodeDesc == null) {
                        if (isDefaultVoid) null else this.name
                    } else if (nodeDesc.isVoid) null
                    else nodeDesc.name

            return base?.let { javaccConfig.nodePrefix + it }
        }


    companion object {

        private fun getJavaClassFromQname(context: JccFile, fqcn: String): PsiClass? {

            // this is mostly a hack...
            val scope = ModuleUtil.findModuleForFile(context)
                ?.let { GlobalSearchScope.moduleScope(it) }
                ?: GlobalSearchScope.allScope(context.project)

            return JavaPsiFacade.getInstance(context.project).findClass(fqcn, scope)
        }
    }
}