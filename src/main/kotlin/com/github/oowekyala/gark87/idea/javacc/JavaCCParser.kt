package com.github.oowekyala.gark87.idea.javacc

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

/**
 * @author gark87
 */
class JavaCCParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        val javacc = JavaCC(builder)
        javacc.javacc_input()
        if (!builder.eof()) {
            val errorMark = builder.mark()
            while (!builder.eof()) {
                builder.advanceLexer()
            }
            errorMark.error("Extra text")
        }
        rootMarker.done(root)
        return builder.treeBuilt
    }
}
