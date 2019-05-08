package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.ijcc.lang.model.parserQualifiedName
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.util.removeLast
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.OptsModelImpl
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
 * @property class VBean for the class
 * @property superNode Node bean describing the node which this node extends directly, as defined by [JjtxOptsModel.typeHierarchy]
 * @property subNodes List of node beans for which [superNode] is this
 */
data class NodeVBean(
    val name: String,
    val `class`: ClassVBean,
    val superNode: NodeVBean?,
    val subNodes: List<NodeVBean>
) : TreeOps<NodeVBean> {

    override val adapter: TreeLikeAdapter<NodeVBean> = TreeLikeWitness

    val klass = `class`

    init {

        superNode?.let {
            it.subNodes.let { it as MutableList } += this
        }
    }

    // those methods should not include both subNodes & supernodes,
    // otherwise we run into infinite loop

    override fun toString(): String = "NodeBean(${`class`.qualifiedName})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NodeVBean

        if (name != other.name) return false
        if (`class` != other.`class`) return false
        if (superNode != other.superNode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + `class`.hashCode()
        return result
    }


    companion object {

        internal object TreeLikeWitness : DoublyLinkedTreeLikeAdapter<NodeVBean> {
            override fun nodeName(node: NodeVBean): String = node.name

            override fun getChildren(node: NodeVBean): List<NodeVBean> = node.subNodes

            override fun getParent(node: NodeVBean): NodeVBean? = node.superNode
        }

        // stack has the parent of the receiver
        private fun TypeHierarchyTree.dumpInternal(ctx: JjtxContext,
                                                   stack: MutableList<NodeVBean>,
                                                   total: MutableList<NodeVBean>) {

            val simpleName = nodeName.substringAfterLast('.')

            val name = when (specificity) {
                Specificity.RESOLVED,
                Specificity.REGEX -> simpleName.removePrefix(ctx.jjtxOptsModel.nodePrefix)
                else              -> simpleName
            }

            val bean = NodeVBean(
                name = name,
                `class` = ClassVBean(nodeName),
                superNode = stack.lastOrNull(),
                subNodes = mutableListOf()
            )

            stack += bean
            total += bean

            children.forEach { it.dumpInternal(ctx, stack, total) }

            stack.removeLast()
        }


        internal fun toBean(tree: TypeHierarchyTree, ctx: JjtxContext): NodeVBean {
            val total = mutableListOf<NodeVBean>()
            tree.dumpInternal(ctx, mutableListOf(), total)
            return total.first()
        }
    }
}

/**
 * Represents a file.
 */
data class FileVBean(
    val fileName: String,
    val absolutePath: String,
    val extension: String?
) {
    companion object {
        fun create(jccFile: JccFile): FileVBean = FileVBean(
            fileName = jccFile.name,
            absolutePath = jccFile.virtualFile.path,
            extension = jccFile.virtualFile.extension
        )
    }
}

data class VisitorVBean(
    val id: String,
    val `class`: ClassVBean,
    val context: Map<String, Any?>
) {

    companion object {
        fun fromVisitorBean(id: String, bean: VisitorConfigBean) = VisitorVBean(
            id = id,
            `class` = ClassVBean(bean.genClassName!!),
            context = bean.context ?: emptyMap()
        )
    }

}


data class ClassVBean(
    val qualifiedName: String
) {
    val simpleName: String
    val `package`: String

    init {
        val (pack, n) = qualifiedName.splitAroundLast('.', firstBias = false)

        simpleName = n
        `package` = pack
    }
}

data class ParserVBean(
    val `class`: ClassVBean
)

data class RunVBean(
    val visitors: Map<String, VisitorVBean>
) {
    companion object {
        fun create(ctx: JjtxContext): RunVBean {
            val visitors =
                ctx.jjtxOptsModel
                    .let { it as? OptsModelImpl }
                    ?.visitorBeans
                    ?.filterValues { it.execute ?: true }
                    ?.mapValues { VisitorVBean.fromVisitorBean(it.key, it.value) }
                    ?: emptyMap()

            return RunVBean(
                visitors = visitors
            )
        }
    }
}

/**
 * Presents information about the whole grammar.
 *
 * @property name The name of the grammar
 * @property file A [FileVBean] describing the main grammar file
 * @property nodePackage The package in which the nodes live, as defined by [JjtxOptsModel.nodePackage]
 * @property typeHierarchy The list of all [NodeVBean]s defined in the grammar
 */
data class GrammarVBean(
    val name: String,
    val file: FileVBean,
    val nodePackage: String,
    val parser: ParserVBean,
    val rootNode: NodeVBean,
    val typeHierarchy: List<NodeVBean>
    ) {

    companion object {
        fun create(ctx: JjtxContext): GrammarVBean {
            val visitors =
                ctx.jjtxOptsModel
                    .let { it as? OptsModelImpl }
                    ?.visitorBeans
                    ?.filterValues { it.execute ?: true }
                    ?.mapValues { VisitorVBean.fromVisitorBean(it.key, it.value) }
                    ?: emptyMap()

            return GrammarVBean(
                name = ctx.grammarName,
                file = FileVBean.create(ctx.grammarFile),
                nodePackage = ctx.jjtxOptsModel.nodePackage,
                parser = ParserVBean(ClassVBean(ctx.jjtxOptsModel.inlineBindings.parserQualifiedName)),
                rootNode = ctx.jjtxOptsModel.typeHierarchy,
                typeHierarchy = ctx.jjtxOptsModel.typeHierarchy.descendantsOrSelf().toList()
            )
        }
    }
}


operator fun VelocityContext.set(key: String, value: Any) {
    put(key, value)
}
