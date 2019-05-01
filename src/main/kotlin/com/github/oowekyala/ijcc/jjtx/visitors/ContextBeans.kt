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
    val root: Boolean = superNode == null

    companion object {

        // stack has the parent of the receiver
        private fun TypeHierarchyTree.dumpInternal(ctx: JjtxRunContext,
                                                   stack: MutableList<NodeBean>,
                                                   total: MutableList<NodeBean>) {

            val (pack, simpleName) = nodeName.splitAroundLast('.')

            val name = when (specificity) {
                Specificity.RESOLVED -> simpleName.removePrefix(ctx.jjtxOptsModel.nodePrefix)
                else                 -> simpleName
            }

            val bean = NodeBean(
                name = name,
                classSimpleName = simpleName,
                classPackage = pack,
                classQualifiedName = nodeName,
                superNode = stack.lastOrNull()
            )

            stack += bean
            total += bean

            children.forEach { it.dumpInternal(ctx, stack, total) }

            stack.removeLast()
        }


        fun dump(tree: TypeHierarchyTree, ctx: JjtxRunContext): List<NodeBean> {

            val total = mutableListOf<NodeBean>()
            tree.dumpInternal(ctx, mutableListOf(), total)
            return total
        }
    }
}

data class GrammarBean(
    val name: String,
    val file: File,
    val nodePackage: String,
    val typeHierarchy: List<NodeBean>
) {

    companion object {
        fun create(ctx: JjtxRunContext): GrammarBean = GrammarBean(
            name = ctx.jjtxParams.grammarName,
            file = ctx.jjtxParams.mainGrammarFile!!,
            nodePackage = ctx.jjtxOptsModel.nodePackage,
            typeHierarchy = NodeBean.dump(ctx.jjtxOptsModel.typeHierarchy, ctx)
        )
    }
}


operator fun VelocityContext.set(key: String, value: Any) {
    put(key, value)
}
