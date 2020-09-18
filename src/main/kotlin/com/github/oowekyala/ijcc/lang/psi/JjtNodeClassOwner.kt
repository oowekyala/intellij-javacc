package com.github.oowekyala.ijcc.lang.psi

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

    // TODO 21 regex specs

    val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor?

    @JvmDefault
    val isVoid: Boolean
        get() = nodeRawName == null


    /** Gets the node's class qualified name. */
    val nodeQualifiedName: String?

    /** Gets the node's simple name, accounting for the node prefix, etc. */
    val nodeSimpleName: String?

    /** Name without the prefixes. */
    val nodeRawName: String?

}


val JjtNodeClassOwner.isNotVoid: Boolean
    get() = !isVoid



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

