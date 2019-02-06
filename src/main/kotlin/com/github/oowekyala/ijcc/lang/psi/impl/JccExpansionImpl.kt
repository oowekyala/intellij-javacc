// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

open class JccExpansionImpl(node: ASTNode) : JccPsiElementImpl(node), JccExpansion {

    open fun accept(visitor: JccVisitor) {
        visitor.visitExpansion(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    /**
     * Propagates deletion to keep the AST in a consistent state.
     */
    override fun delete() {

        val parent = parent

        when (parent) {
            is JccExpansionSequence    -> {
                val siblings = parent.expansionUnitList
                val numSiblings = siblings.size

                // no expansion sequence has just one child
                require(numSiblings >= 2)

                when {
                    numSiblings == 2 -> {
                        // replace parent with sibling
                        val sibling = siblings.minus(this).first()

                        parent.replace(sibling)
                    }
                    numSiblings > 2  -> super.delete() // delete just this
                }
            }
            is JccExpansionAlternative                                  -> {
                require(this is JccExpansionSequenceOrUnit)

                val expansions = parent.expansionList
                val numSiblings = expansions.size
                val myIdx = expansions.indexOf(this)

                // no expansion alternative has just one child
                require(numSiblings >= 2)
                require(myIdx >= 0)


                when {
                    numSiblings == 2 -> {
                        // replace parent with sibling
                        val sibling = expansions.minus(this).first()
                        parent.replace(sibling)
                    }
                    // numSiblings > 2
                    myIdx == 0       -> {
                        // the first, delete the next |
                        val nextUnion = nextSiblingNoWhitespace!!
                        parent.deleteChildRange(this, nextUnion)
                    }
                    myIdx > 0        -> {
                        // (a | b | c)
                        // not the first
                        // delete the previous | as well
                        val prevUnion = prevSiblingNoWhitespace!!
                        parent.deleteChildRange(prevUnion, this)
                    }
                }
            }
            is JccLocalLookaheadUnit -> {
                // this is a syntactic lookahead, we have to delete the commas too if any

                when {

                    parent.isSemantic && !parent.isLexical -> {
                        // comma to the right

                        val rightComma = nextSiblingNoWhitespace!!
                        parent.deleteChildRange(this, rightComma)
                    }

                    parent.isLexical                       -> {
                        // comma to the left
                        val leftComma = prevSiblingNoWhitespace!!
                        parent.deleteChildRange(leftComma, this)
                    }

                    else                                   -> {
                        // just syntactic? then nothing more to do.
                        // the LOOKAHEAD will be left with empty parens
                        super.delete()
                    }
                }
            }
            else                       -> super.delete()
        }
    }

}
