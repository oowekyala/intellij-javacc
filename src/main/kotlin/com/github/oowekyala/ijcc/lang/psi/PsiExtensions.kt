package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.util.prepend
import com.github.oowekyala.ijcc.util.takeUntil
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil

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

fun PsiElement.descendantSequence(includeSelf: Boolean,
                                  reversed: Boolean = false,
                                  depthFirst: Boolean = false): Sequence<PsiElement> =
    descendantSequence(reversed, depthFirst).let {
        when {
            includeSelf -> it.prepend(this)
            else        -> it
        }
    }


/** Lazy sequence of siblings.
 *
 * @param forward If true the sequence iterates on the following siblings, otherwise on the previous siblings
 */
fun PsiElement.siblingSequence(forward: Boolean) =
    if (forward) generateSequence(this.nextSibling) { it.nextSibling }
    else generateSequence(this.prevSibling) { it.prevSibling }

/** Returns true if the node's token type is [TokenType.WHITE_SPACE]. */
val PsiElement.isWhitespace: Boolean
    get() = isOfType(TokenType.WHITE_SPACE)

/** Returns true if the node's token type is [TokenType.ERROR_ELEMENT]. */
val PsiElement.isError
    get() = isOfType(TokenType.ERROR_ELEMENT)

val PsiElement.prevSiblingNoWhitespace: PsiElement?
    get() = siblingSequence(forward = false).firstOrNull { !it.isWhitespace }

val PsiElement.nextSiblingNoWhitespace: PsiElement?
    get() = siblingSequence(forward = true).firstOrNull { !it.isWhitespace }

val PsiElement.lastChildNoWhitespace: PsiElement?
    inline get() = childrenSequence(reversed = true).firstOrNull { !it.isWhitespace }

val PsiElement.lastChildNoError: PsiElement?
    inline get() = childrenSequence(reversed = true).firstOrNull { !it.isError }


fun PsiElement.astChildrenSequence(reversed: Boolean = false,
                                   filter: TokenSet = TokenSet.ANY): Sequence<PsiElement> =
    when (reversed) {
        false -> node.getChildren(filter).mapTo(mutableListOf()) { it.psi }.asSequence().filterNotNull()
        true  -> {
            val children = node.getChildren(filter)
            var i = children.size
            sequence {
                while (i > 0) {
                    val child = children[--i].psi
                    if (child != null) yield(child)
                }
            }
        }
    }

/** Parent sequence, stopping at the file node. */
fun PsiElement.ancestors(includeSelf: Boolean) =
    generateSequence(if (includeSelf) this else parent) { it.parent }.takeWhile { it !is PsiDirectory }

inline fun <reified T : PsiElement> PsiElement.firstAncestorOrNull(includeSelf: Boolean = false): T? =
    ancestors(includeSelf).filterIsInstance<T>().firstOrNull()

val PsiElement.textRangeInParent: TextRange
    get() {
        val offset = startOffsetInParent
        return TextRange(offset, offset + textLength)
    }

fun PsiElement.innerRange(from: Int = 0, endOffset: Int = 0): TextRange = TextRange(from, textLength - endOffset)

fun PsiElement.rangeInParent(f: (PsiElement) -> TextRange) = f(this).relativize(parent.textRange)

/** Returns this text range as seen from the [container] range. */
fun TextRange.relativize(container: TextRange): TextRange? =
    when {
        startOffset < container.startOffset || endOffset > container.endOffset -> null
        else                                                                   ->
            (startOffset - container.startOffset).let { TextRange(it, it + length) }
    }

fun PsiElement.siblingRangeTo(brother: PsiElement): Sequence<PsiElement> =
    if (this.parent !== brother.parent) throw IllegalArgumentException()
    else when {
        this === brother                                       ->
            sequenceOf(this)
        this.startOffsetInParent < brother.startOffsetInParent ->
            this.siblingSequence(forward = true).takeUntil(brother).prepend(this)
        else                                                   ->
            this.siblingSequence(forward = false).takeUntil(brother).prepend(this)

    }

inline fun <reified T : PsiElement> PsiElement.ancestorOrSelf(): T? =
    PsiTreeUtil.getParentOfType(this, T::class.java, /* strict */ false)


fun PsiElement.isOfType(elementType: IElementType): Boolean = node?.elementType == elementType
fun PsiElement.isOfType(elementType: IElementType, vararg rest: IElementType): Boolean =
    node?.elementType.let { TokenSet.create(elementType, *rest).contains(it) }


fun TextRange.containsInside(offset: Int): Boolean = offset in (startOffset + 1)..(endOffset - 1)


val PsiElement.lineNumber
    get() = StringUtil.offsetToLineNumber(containingFile.text, textOffset)