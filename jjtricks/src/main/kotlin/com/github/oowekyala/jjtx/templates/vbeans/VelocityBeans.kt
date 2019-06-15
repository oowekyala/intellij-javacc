package com.github.oowekyala.jjtx.templates.vbeans

import com.github.oowekyala.ijcc.lang.model.parserQualifiedName
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.modelOption
import com.github.oowekyala.ijcc.util.removeLast
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.preprocessor.JavaccGenOptions
import com.github.oowekyala.jjtx.templates.FileGenTask
import com.github.oowekyala.jjtx.typeHierarchy.Specificity
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.util.TreeOps
import com.github.oowekyala.jjtx.util.extension
import com.github.oowekyala.jjtx.util.splitAroundLast
import com.github.oowekyala.treeutils.DoublyLinkedTreeLikeAdapter
import com.github.oowekyala.treeutils.TreeLikeAdapter
import java.nio.file.Path
import java.nio.file.Paths

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
 * @property external True if the node matches no production of the grammar. In which case the default node factory has no constructor for it
 */
data class NodeVBean(
    val name: String,
    val `class`: ClassVBean,
    val superNode: NodeVBean?,
    val subNodes: List<NodeVBean>,
    val external: Boolean
) : TreeOps<NodeVBean> {

    override val adapter: TreeLikeAdapter<NodeVBean> =
        TreeLikeWitness

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
                subNodes = mutableListOf(),
                external = isExternal
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
    val absolutePath: String
) {

    val fileName: String
    val extension: String?

    init {
        val p = Paths.get(absolutePath)
        fileName = p.fileName.toString()
        extension = p.extension
    }

    companion object {
        fun create(jccFile: JccFile): FileVBean =
            FileVBean(absolutePath = jccFile.virtualFile.path)

        operator fun invoke(absolutePath: Path): FileVBean =
            FileVBean(absolutePath = absolutePath.toString())
    }
}

data class FileGenVBean(
    val id: String,
    val `class`: ClassVBean,
    val context: Map<String, Any?>
) {

    companion object {
        fun fromGenTask(id: String, bean: FileGenTask) =
            FileGenVBean(
                id = id,
                `class` = ClassVBean(bean.genFqcn),
                context = bean.context
            )
    }

}

data class JjtricksGenVBean(
    val support: Map<String, FileGenVBean>
) {
    companion object {
        fun create(opts: JavaccGenOptions) = JjtricksGenVBean(
            support = opts.supportFiles.mapValues { (id, fileGen) -> FileGenVBean.fromGenTask(id, fileGen) }
        )
    }
}


data class ClassVBean(
    val qualifiedName: String
) {
    val simpleName: String
    val `package`: String

    fun siblingClass(simpleName: String) =
        ClassVBean(
            if (`package`.isEmpty()) simpleName
            else "$`package`.$simpleName"
        )

    override fun toString(): String = qualifiedName

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
    val commonGen: Map<String, FileGenVBean>,
    val javaccGen: JjtricksGenVBean
) {
    companion object {
        fun create(ctx: JjtxContext): RunVBean {
            val visitors =
                ctx.jjtxOptsModel.commonGen.mapValues { (id, task) ->
                    FileGenVBean.fromGenTask(id, task)
                }

            return RunVBean(
                commonGen = visitors,
                javaccGen = JjtricksGenVBean.create(ctx.jjtxOptsModel.javaccGen)
            )
        }
    }
}

/**
 * Presents information about the whole grammar.
 *
 * @property name The name of the grammar
 * @property grammarFile A [FileVBean] describing the main grammar file
 * @property nodePackage The package in which the nodes live, as defined by [JjtxOptsModel.nodePackage]
 * @property typeHierarchy The list of all [NodeVBean]s defined in the grammar
 */
data class GrammarVBean(
    val name: String,
    val grammarFile: FileVBean,
    val nodePackage: String,
    val nodePrefix: String,
    val isTrackTokens: Boolean,
    val nodeTakesParserArg: Boolean,
    val parser: ParserVBean,
    val optionsOfGrammarFile: Map<String, Any?>,
    val rootNode: NodeVBean,
    val typeHierarchy: List<NodeVBean>
) {

    companion object {
        fun create(ctx: JjtxContext): GrammarVBean {

            return GrammarVBean(
                name = ctx.grammarName,
                grammarFile = FileVBean.create(ctx.grammarFile),
                nodePackage = ctx.jjtxOptsModel.nodePackage,
                nodePrefix = ctx.jjtxOptsModel.nodePrefix,
                isTrackTokens = ctx.jjtxOptsModel.isTrackTokens,
                parser = ParserVBean(
                    ClassVBean(
                        ctx.jjtxOptsModel.inlineBindings.parserQualifiedName
                    )
                ),
                optionsOfGrammarFile = ctx.jjtxOptsModel.inlineBindings.allOptionsBindings.associate {
                    // FIXME possible problems here, must first validate inline options
                    Pair(it.name, it.modelOption!!.getValue(it, ctx.jjtxOptsModel.inlineBindings))
                },
                nodeTakesParserArg = ctx.jjtxOptsModel.nodeTakesParserArg,
                rootNode = ctx.jjtxOptsModel.typeHierarchy,
                typeHierarchy = ctx.jjtxOptsModel.typeHierarchy.descendantsOrSelf().toList()
            )
        }
    }
}


