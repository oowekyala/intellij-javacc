package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.ijcc.lang.model.parserQualifiedName
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.util.removeLast
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.typeHierarchy.Specificity
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.util.TreeOps
import com.github.oowekyala.jjtx.util.splitAroundLast
import com.github.oowekyala.treeutils.DoublyLinkedTreeLikeAdapter
import com.github.oowekyala.treeutils.TreeLikeAdapter
import org.apache.velocity.VelocityContext

/*
    Beans used to present data inside a Velocity context.
 */



/**
 * Represents a JJTree node.
 *
 * @property name Simple name of the node, unprefixed, as occurring in the grammar
 * @property classSimpleName Simple name of the class (prefixed)
 * @property classQualifiedName Fully qualified name of the node class ([classPackage] + [classSimpleName])
 * @property classPackage Package name of the class (may be the empty string)
 * @property superNode Node bean describing the node which this node extends directly, as defined by [JjtxOptsModel.typeHierarchy]
 * @property subNodes List of node beans for which [superNode] is this
 */
data class NodeBean(
    val name: String,
    val classQualifiedName: String,
    val superNode: NodeBean?,
    val subNodes: List<NodeBean>
) : TreeOps<NodeBean> {

    val classSimpleName: String
    val classPackage: String

    override val adapter: TreeLikeAdapter<NodeBean> = TreeLikeWitness

    init {

        val (pack, simpleName) = classQualifiedName.splitAroundLast('.', firstBias = false)

        classSimpleName = simpleName
        classPackage = pack

        superNode?.let {
            it.subNodes.let { it as MutableList } += this
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

        internal object TreeLikeWitness : DoublyLinkedTreeLikeAdapter<NodeBean> {
            override fun nodeName(node: NodeBean): String = node.name

            override fun getChildren(node: NodeBean): List<NodeBean> = node.subNodes

            override fun getParent(node: NodeBean): NodeBean? = node.superNode
        }

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


        internal fun toBean(tree: TypeHierarchyTree, ctx: JjtxContext): NodeBean {
            val total = mutableListOf<NodeBean>()
            tree.dumpInternal(ctx, mutableListOf(), total)
            return total.first()
        }
    }
}

/**
 * Represents a file.
 */
data class FileBean(
    val fileName: String,
    val absolutePath: String,
    val extension: String?
) {
    companion object {
        fun create(jccFile: JccFile): FileBean = FileBean(
            fileName = jccFile.name,
            absolutePath = jccFile.virtualFile.path,
            extension = jccFile.virtualFile.extension
        )
    }
}

/**
 * Presents information about the whole grammar.
 *
 * @property name The name of the grammar
 * @property file A [FileBean] describing the main grammar file
 * @property nodePackage The package in which the nodes live, as defined by [JjtxOptsModel.nodePackage]
 * @property typeHierarchy The list of all [NodeBean]s defined in the grammar
 */
data class GrammarBean(
    val name: String,
    val file: FileBean,
    val nodePackage: String,
    val parserQualifiedName: String,
    val rootNode: NodeBean,
    val typeHierarchy: List<NodeBean>
) {


    val parserSimpleName: String
    val parserPackage: String

    init {

        val (pack, simpleName) = parserQualifiedName.splitAroundLast('.', firstBias = false)

        parserSimpleName = simpleName
        parserPackage = pack

    }


    companion object {
        fun create(ctx: JjtxContext): GrammarBean = GrammarBean(
            name = ctx.grammarName,
            file = FileBean.create(ctx.grammarFile),
            nodePackage = ctx.jjtxOptsModel.nodePackage,
            parserQualifiedName = ctx.jjtxOptsModel.inlineBindings.parserQualifiedName,
            rootNode = ctx.jjtxOptsModel.typeHierarchy,
            typeHierarchy = ctx.jjtxOptsModel.typeHierarchy.descendantsOrSelf().toList()
        )
    }
}


operator fun VelocityContext.set(key: String, value: Any) {
    put(key, value)
}
