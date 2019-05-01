package com.github.oowekyala.jjtx

import com.github.oowekyala.treeutils.TreeLikeAdapter
import com.github.oowekyala.treeutils.printers.TreePrinter

/**
 * @author Clément Fournier
 */
class SimpleTreePrinter<in H : Any>(private val treeLikeAdapter: TreeLikeAdapter<H>) : TreePrinter<H> {


    /**
     * Dumps the given subtree to a string.
     *
     * @param node Subtree to dump
     * @param maxDumpDepth Maximum depth on which to recurse.
     * A value of zero only dumps the root node. A negative value
     * dumps the whole subtree.
     */
    override fun dumpSubtree(node: H, maxDumpDepth: Int): String = StringBuilder().also {
        printDirectoryTree(node, 0, it)
    }.toString()

    private fun printDirectoryTree(node: H, indent: Int,
                                   sb: StringBuilder) {
        sb.append(getIndentString(indent))
        sb.append("+--")
        sb.append(treeLikeAdapter.nodeName(node))
        sb.append("/")
        sb.append("\n")
        for (child in treeLikeAdapter.getChildren(node)) {
            if (treeLikeAdapter.getChildren(node).isNotEmpty()) {
                printDirectoryTree(child, indent + 1, sb)
            } else {
                printFile(child, indent + 1, sb)
            }
        }

    }

    private fun printFile(node: H, indent: Int, sb: StringBuilder) {
        sb.append(getIndentString(indent))
        sb.append("+--")
        sb.append(treeLikeAdapter.nodeName(node))
        sb.append("\n")
    }

    private fun getIndentString(indent: Int): String {
        val sb = StringBuilder()
        for (i in 0 until indent) {
            sb.append("|  ")
        }
        return sb.toString()
    }
}
