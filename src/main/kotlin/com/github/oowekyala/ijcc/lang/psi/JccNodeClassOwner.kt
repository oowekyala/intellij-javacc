package com.github.oowekyala.ijcc.lang.psi

import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope

/**
 * Node that is tied to a generated node class, assumed to be
 * somewhere in the project.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccNodeClassOwner : JccPsiElement, JccIdentifierOwner {

    val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor?

}


val JccNodeClassOwner.isVoid: Boolean
    get() = nodeIdentifier == null

val JccNodeClassOwner.isNotVoid: Boolean
    get() = !isVoid

/** Gets the Psi class representing the node's class for navigation. */
val JccNodeClassOwner.nodeClass: PsiClass?
    get() = nodeQualifiedName?.let {
        getJavaClassFromQname(containingFile, it)
    }


/** Gets the node's class qualified name. */
val JccNodeClassOwner.nodeQualifiedName: String?
    get() = nodeSimpleName?.let {
        val packageName = grammarOptions.nodePackage

        if (packageName.isEmpty()) nodeSimpleName
        else "$packageName.$it"
    }

/** Gets the node's simple name, accounting for the node prefix, etc. */
val JccNodeClassOwner.nodeSimpleName: String?
    get() = nodeIdentifier?.let { grammarOptions.nodePrefix + it.name }

/** Name without the prefixes. */
val JccNodeClassOwner.rawName: String?
    get() = nodeIdentifier?.name

/**
 * Returns the identifier giving its name to the JJTree node.
 * It is null if the production or scoped expansion is #void.
 * It may differ from [JccNodeClassOwner.getNameIdentifier] on
 * productions that have an annotation renaming them.
 */
val JccNodeClassOwner.nodeIdentifier: JccIdentifier?
    get() = when (this) {
        is JccScopedExpansionUnit   -> jjtreeNodeDescriptor.nameIdentifier
        is JccNonTerminalProduction -> jjtreeNodeDescriptor.let {
            if (it == null)
                if (grammarOptions.isDefaultVoid) null
                else this.nameIdentifier
            else it.nameIdentifier
        }
        else                        -> throw IllegalStateException()
    }

val JccIdentifier.isJjtreeNodeIdentifier: Boolean
    get() = parent is JccJjtreeNodeDescriptor
        || (parent as? JccJavaNonTerminalProductionHeader)
        ?.let { it.parent as JccNonTerminalProduction }
        ?.let { it.nameIdentifier == it.nodeIdentifier } == true


fun getJavaClassFromQname(context: JccFile, fqcn: String): PsiClass? {

    // this is mostly a hack... Idk how to search in all places
    val scope = ModuleUtil.findModuleForFile(context)
        ?.let { GlobalSearchScope.moduleScope(it) }
        ?: GlobalSearchScope.allScope(context.project)

    return JavaPsiFacade.getInstance(context.project).findClass(fqcn, scope)
}
