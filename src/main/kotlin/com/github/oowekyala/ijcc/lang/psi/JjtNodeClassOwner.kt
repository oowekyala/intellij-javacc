package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope

/**
 * Node that is tied to a generated node class, assumed to be
 * somewhere in the project. This is used to represent the
 * partial declarations of JJTree nodes. The methods return
 * non-null only if this psi element is associated with a
 * node, i.e. we're in a JJTree file and the node is not void.
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
    val nodeQualifiedName: String?

    /** Gets the node's simple name, accounting for the node prefix, etc. */
    val nodeSimpleName: String?

    /** Name without the prefixes. */
    val nodeRawName: String?

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
