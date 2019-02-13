package com.github.oowekyala.ijcc.lang.psi

import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope

/**
 * Node that is tied to a generated node class, assumed to be
 * somewhere in the project. This is mostly used to represent
 * the partial declarations of JJTree nodes.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JjtNodeClassOwner : JccPsiElement, JccIdentifierOwner {

    val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor?

    @JvmDefault
    val isVoid: Boolean
        get() = nodeIdentifier == null


    /** Gets the node's class qualified name. */
    @JvmDefault
    val nodeQualifiedName: String?
        get() = nodeSimpleName?.let {
            val packageName = grammarOptions.nodePackage

            if (packageName.isEmpty()) nodeSimpleName
            else "$packageName.$it"
        }

    /** Gets the node's simple name, accounting for the node prefix, etc. */
    @JvmDefault
    val nodeSimpleName: String?
        get() = nodeIdentifier?.let { grammarOptions.nodePrefix + it.name }

    /** Name without the prefixes. */
    @JvmDefault
    val rawName: String?
        get() = nodeIdentifier?.name


}


val JjtNodeClassOwner.isNotVoid: Boolean
    get() = !isVoid

/** Gets the Psi class representing the node's class for navigation. */
val JjtNodeClassOwner.nodeClass: PsiClass?
    get() = nodeQualifiedName?.let {
        getJavaClassFromQname(containingFile, it)
    }


/**
 * Returns the identifier giving its name to the JJTree node.
 * It is null if the production or scoped expansion is #void.
 * It may differ from [JjtNodeClassOwner.getNameIdentifier] on
 * productions that have an annotation renaming them.
 */
val JjtNodeClassOwner.nodeIdentifier: JccIdentifier?
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


fun getJavaClassFromQname(context: JccFile, fqcn: String): PsiClass? =
    JavaPsiFacade.getInstance(context.project).findClass(fqcn, context.grammarSearchScope)


/**
 * Scope in which a grammar file will be searched for a matching
 * JJTree declaration. This is also used to search for java files
 * from the grammar file.
 */
val PsiFile.grammarSearchScope: GlobalSearchScope
    get() = GlobalSearchScope.projectScope(project)
