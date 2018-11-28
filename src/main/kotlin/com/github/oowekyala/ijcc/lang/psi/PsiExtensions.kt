package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType

/** Lazy sequence of children. */
fun PsiElement.childrenSequence(reversed: Boolean = false): Sequence<PsiElement> = when (reversed) {
    false -> children.asSequence()
    true  -> {
        val children = children
        var i = children.size
        sequence {
            while (i > 0) {
                val child = children[--i]
                yield(child)
            }
        }
    }
}

/** Lazy sequence of all descendants.
 *
 * @param reversed If true the children are returned in the reversed order
 * @param depthFirst Perform a depth first traversal. Default is false, i.e. breadth-first
 */
fun PsiElement.descendantSequence(reversed: Boolean = false, depthFirst: Boolean = false): Sequence<PsiElement> {
    val children = childrenSequence(reversed)
    return when (depthFirst) {
        true  -> children.flatMap { sequenceOf(it) + it.descendantSequence(reversed, depthFirst) }
        false -> children + children.flatMap { it.descendantSequence(reversed, depthFirst) }
    }
}

/** Lazy sequence of siblings.
  *
  * @param forward If true the sequence iterates on the following siblings, otherwise on the previous siblings
  */
fun PsiElement.siblingSequence(forward: Boolean) =
        if (forward) generateSequence(this, PsiElement::getNextSibling)
        else generateSequence(this, PsiElement::getPrevSibling)

/** Returns true if the node's token type is [TokenType.WHITE_SPACE]. */
val PsiElement.isWhitespace: Boolean
    get() = node.elementType == TokenType.WHITE_SPACE

val PsiElement.prevSiblingNoWhitespace: PsiElement?
    inline get() = siblingSequence(forward = false).firstOrNull { !it.isWhitespace }

val PsiElement.lastChildNoWhitespace: PsiElement?
    inline get() = childrenSequence(reversed = true).firstOrNull { !it.isWhitespace }