package com.github.oowekyala.ijcc.jjtx.visitors

import com.github.oowekyala.ijcc.jjtx.JjtxRunContext
import com.github.oowekyala.ijcc.jjtx.splitAroundLast
import com.github.oowekyala.ijcc.jjtx.typeHierarchy.Specificity
import com.github.oowekyala.ijcc.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.ijcc.util.removeLast
import org.apache.velocity.VelocityContext
import java.io.File


data class NodeBean(
    val name: String,
    val classSimpleName: String,
    val classQualifiedName: String,
    val classPackage: String,
    val superNode: NodeBean?
) {
    val isRoot get() = superNode == null

    companion object {


        fun dump(tree: TypeHierarchyTree, ctx: JjtxRunContext): List<NodeBean> {

            // stack has the parent of the receiver
            fun TypeHierarchyTree.dumpInternal(stack: MutableList<NodeBean>) {

                val (pack, simpleName) = tree.nodeName.splitAroundLast('.')

                val name = when (tree.specificity) {
                    Specificity.RESOLVED -> simpleName.removePrefix(ctx.jjtxOptsModel.nodePrefix)
                    else                 -> simpleName
                }

                stack += NodeBean(
                    name = name,
                    classSimpleName = simpleName,
                    classPackage = pack,
                    classQualifiedName = tree.nodeName,
                    superNode = stack.lastOrNull()
                )

                children.forEach { it.dumpInternal(stack) }

                stack.removeLast()
            }

            return mutableListOf<NodeBean>().also {
                tree.dumpInternal(it)
            }
        }
    }
}

data class GrammarBean(
    val name: String,
    val file: File
)


operator fun VelocityContext.set(key: String, value: Any) {
    put(key, value)
}
