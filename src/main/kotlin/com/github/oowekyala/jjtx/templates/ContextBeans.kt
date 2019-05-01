package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.util.removeLast
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.splitAroundLast
import com.github.oowekyala.jjtx.typeHierarchy.Specificity
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import org.apache.velocity.VelocityContext


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
        private fun TypeHierarchyTree.dumpInternal(ctx: JjtxContext,
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


        fun dump(tree: TypeHierarchyTree, ctx: JjtxContext): List<NodeBean> {

            val total = mutableListOf<NodeBean>()
            tree.dumpInternal(ctx, mutableListOf(), total)
            return total
        }
    }
}

data class GrammarBean(
    val name: String,
    val file: JccFile,
    val nodePackage: String,
    val typeHierarchy: List<NodeBean>
) {

    companion object {
        fun create(ctx: JjtxContext): GrammarBean = GrammarBean(
            name = ctx.grammarName,
            file = ctx.grammarFile,
            nodePackage = ctx.jjtxOptsModel.nodePackage,
            typeHierarchy = NodeBean.dump(ctx.jjtxOptsModel.typeHierarchy, ctx)
        )
    }
}


operator fun VelocityContext.set(key: String, value: Any) {
    put(key, value)
}
