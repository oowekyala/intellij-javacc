package com.github.oowekyala.jjtx.testutil

import com.github.oowekyala.jjtx.util.dataAst.DataAstNode
import com.github.oowekyala.jjtx.util.dataAst.prettyPrint
import com.github.oowekyala.treeutils.printers.TreePrinter

/**
 * @author Clément Fournier
 */
object DataTreePrinter : TreePrinter<DataAstNode> {
    override fun dumpSubtree(node: DataAstNode, maxDumpDepth: Int): String = node.prettyPrint()
}
