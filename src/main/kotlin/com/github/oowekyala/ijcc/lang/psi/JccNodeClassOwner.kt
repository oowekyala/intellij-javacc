package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.JavaccConfig
import com.github.oowekyala.ijcc.util.EnclosedLogger
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

    @JvmDefault
    fun getNodeClass(javaccConfig: JavaccConfig): NavigatablePsiElement? = when (this) {
        is JccNonTerminalProduction -> getNodeClassImpl(javaccConfig)
        is JccJjtreeNodeDescriptor  -> getNodeClassImpl(javaccConfig)
        else                        -> throw IllegalStateException("JccNodeClassOwner unimplemented for ${this.javaClass}")
    }

    companion object {

        private fun JccNonTerminalProduction.getNodeClassImpl(javaccConfig: JavaccConfig): NavigatablePsiElement? {
            val nodeDescriptor = jjtreeNodeDescriptor
            if (nodeDescriptor == null && javaccConfig.isDefaultVoid || nodeDescriptor?.isVoid == true) return null

            val nodePackage = javaccConfig.nodePackage
            val nodeName = javaccConfig.nodePrefix + if (nodeDescriptor != null) nodeDescriptor.name else this.name

            return getJavaClassFromQname(containingFile, "$nodePackage.$nodeName")
        }

        private fun JccJjtreeNodeDescriptor.getNodeClassImpl(javaccConfig: JavaccConfig): NavigatablePsiElement? {
            if (this.isVoid) return null

            val nodePackage = javaccConfig.nodePackage
            val nodeName = javaccConfig.nodePrefix + this.name

            return getJavaClassFromQname(containingFile, "$nodePackage.$nodeName")
        }

        private fun getJavaClassFromQname(context: JccFile, fqcn: String): PsiClass? {

            val scope = ModuleUtil.findModuleForFile(context)
                ?.let { GlobalSearchScope.moduleScope(it) }
                ?: GlobalSearchScope.allScope(context.project)

            return JavaPsiFacade.getInstance(context.project).findClass(fqcn, scope)
        }
    }
}