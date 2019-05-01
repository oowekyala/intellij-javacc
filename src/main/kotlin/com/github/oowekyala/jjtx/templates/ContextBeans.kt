package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.util.removeLast
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.splitAroundLast
import com.github.oowekyala.jjtx.typeHierarchy.Specificity
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import groovyjarjarantlr.build.ANTLR.root
import org.apache.velocity.VelocityContext


data class NodeBean(
    val name: String,
    val classQualifiedName: String,
    val superNode: NodeBean?,
    val subNodes: MutableList<NodeBean>
) {

    val classSimpleName: String
    val classPackage: String

    init {

        val (pack, simpleName) = classQualifiedName.splitAroundLast('.')

        classSimpleName = simpleName
        classPackage = pack

        superNode?.let {
            it.subNodes += this
        }
    }

    // those methods should not include both subNodes & supernodes,
    // otherwise we run into infinite loop
    
    override fun toString(): String = "NodeBean($classQualifiedName)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NodeBean

        if (name != other.name) return false
        if (classQualifiedName != other.classQualifiedName) return false
        if (superNode != other.superNode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + classQualifiedName.hashCode()
        return result
    }


    companion object {

        // stack has the parent of the receiver
        private fun TypeHierarchyTree.dumpInternal(ctx: JjtxContext,
                                                   stack: MutableList<NodeBean>,
                                                   total: MutableList<NodeBean>) {

            val simpleName = nodeName.substringAfterLast('.')

            val name = when (specificity) {
                Specificity.RESOLVED,
                Specificity.REGEX -> simpleName.removePrefix(ctx.jjtxOptsModel.nodePrefix)
                else              -> simpleName
            }

            val bean = NodeBean(
                name = name,
                classQualifiedName = nodeName,
                superNode = stack.lastOrNull(),
                subNodes = mutableListOf()
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
