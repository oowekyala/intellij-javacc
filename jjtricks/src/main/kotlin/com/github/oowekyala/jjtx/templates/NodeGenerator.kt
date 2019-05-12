package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.parse
import com.github.oowekyala.jjtx.reporting.MessageCategory
import com.github.oowekyala.jjtx.util.*
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.util.regex.PatternSyntaxException


//    # Match predicates in order
//    # v:X template X with node generation context
//  nodeTemplates:
//    "v: $node == $grammar.rootNode":
//      - templateFile: "/jjtx/templates/RootInterface.java.vm"
//        formatter: "java"
//      - templateFile: "/jjtx/templates/NodeRoot.java.vm"
//        formatter: "java"
//    "v: !$node.subnodes.empty":
//      - templateFile: "/jjtx/templates/NodeInterface.java.vm"
//        genClassName: "$node.class.qualifiedName"
//        formatter: "java"
//      - templateFile: "/jjtx/templates/NodeAbstractClass.java.vm"
//        genClassName: "${grammar.nodePackage}.Abstract${node.name}"
//        formatter: "java"
//    "r:.*":
//      - templateFile: "/jjtx/templates/Node.java.vm"
//        formatter: "java"


internal fun DataAstNode.toNodeGenerationSchemes(ctx: JjtxContext): Map<String, GrammarGenerationScheme> =
    when (this) {
        is AstMap -> this.mapValues { (id, node) -> node.toSingleNodeGenerationScheme(ctx, id) }
        else      -> {
            ctx.messageCollector.report(
                "Expected map of ids to node generation schemes",
                MessageCategory.WRONG_TYPE,
                position
            )
            emptyMap()
        }
    }

private fun DataAstNode.toSingleNodeGenerationScheme(ctx: JjtxContext, id: String): GrammarGenerationScheme {

    val normalisedMap = when (this) {
        is AstMap    -> this
        is AstSeq    ->
            AstMap(
                map = mapOf("r:.*" to this),
                position = this.position
            )
        is AstScalar -> {
            ctx.messageCollector.reportError("Expected map or sequence", position)
        }
    }

    return normalisedMap.toNodeGenerationSchemeImpl(ctx, id)
}

/**
 * @receiver This is the map of patterns to filegen tasks
 */
private fun AstMap.toNodeGenerationSchemeImpl(ctx: JjtxContext, id: String): GrammarGenerationScheme {

    val found = mutableSetOf<NodeVBean>()

    val allSchemes = mutableListOf<NodeGenerationScheme>()

    val waitingForNextPattern = mutableListOf<NodeVBean>()

    for ((pattern, node) in this) {

        val (alreadyMatched, newMatches) = pattern.findMatchingNodes(ctx, node.position).partition { it in found }

        found += newMatches

        if (alreadyMatched.isNotEmpty()) {
            ctx.messageCollector.report(
                "Pattern '$pattern' matched ${alreadyMatched.size} nodes that are already matched by some rules: "
                    + alreadyMatched.joinToString { it.name },
                MessageCategory.DUPLICATE_MATCH,
                keyPositions[pattern]
            )
        }

        if (newMatches.isEmpty()) {
            ctx.messageCollector.report(
                "Pattern '$pattern' matched no nodes",
                MessageCategory.NO_MATCH,
                keyPositions[pattern]
            )
        }

        val schemes = when (node) {
            is AstMap -> {
                val b = node.parse<NodeGenerationBean>()
                val ms = newMatches + waitingForNextPattern
                waitingForNextPattern.clear()
                listOfNotNull(b.promote(ctx, node.position, ms))
            }
            is AstSeq -> {
                val (maps, notMaps) = node.partition { it is AstMap }

                if (notMaps.isNotEmpty()) {
                    ctx.messageCollector.report(
                        "Expected a map",
                        MessageCategory.WRONG_TYPE,
                        *notMaps.map { it.position }.toTypedArray()
                    )
                }
                val ms = newMatches + waitingForNextPattern
                waitingForNextPattern.clear()

                // handles nothing after colon with empty map I think
                maps.mapNotNull {
                    val b = it.parse<NodeGenerationBean>()
                    b.promote(ctx, it.position, ms)
                }
            }
            is AstScalar -> {
                if (node.type == ScalarType.STRING && node.any.trim().isEmpty()) {
                    waitingForNextPattern += newMatches
                }
                emptyList()
            }
        }

        allSchemes += schemes

    }

    val allBeans = ctx.jjtxOptsModel.typeHierarchy.descendantsOrSelf().toSet()

    val remaining = allBeans - found

    if (remaining.isNotEmpty()) {
        ctx.messageCollector.report(
            "Some nodes matched no generation patterns: " + remaining.joinToString { it.name },
            MessageCategory.UNCOVERED_GEN_NODE,
            position
        )
    }

    return GrammarGenerationScheme(allSchemes, id)
}

fun String.findMatchingNodes(ctx: JjtxContext, positionInfo: Position?): List<NodeVBean> {

    RegexPattern.matchEntire(this)?.groups?.get(1)?.run {
        return findByRegex(ctx, positionInfo, value)
    }

    TemplatePattern.matchEntire(this)?.groups?.get(1)?.run {
        return findByTemplate(ctx, positionInfo, value)
    }

    return ctx.jjtxOptsModel.typeHierarchy.descendantsOrSelf().filter { it.name == this }.toList()
}

private val RegexPattern = Regex("^r:(.*)")
private val TemplatePattern = Regex("^v:(.*)")


private fun findByTemplate(ctx: JjtxContext, positionInfo: Position?, templateStr: String): List<NodeVBean> {

    val engine = VelocityEngine()

    fun NodeVBean.matchesTemplate(): Boolean {

        val vctx = VelocityContext(ctx.globalVelocityContext).also {
            it["node"] = this@matchesTemplate
        }

        val fullTemplate =
            """
            #if ( $templateStr )
              true
            #end
        """.trimIndent()

        return engine.evaluate(vctx, fullTemplate).trim().isNotEmpty()
    }



    return ctx.jjtxOptsModel
        .typeHierarchy
        .descendantsOrSelf()
        .filter { it.matchesTemplate() }
        .toList()
        .also {
        if (it.isEmpty()) {
            ctx.messageCollector.report(
                "Template pattern matches no nodes",
                MessageCategory.UNMATCHED_HIERARCHY_REGEX,
                positionInfo
            )
        }
    }
}

private fun findByRegex(ctx: JjtxContext, positionInfo: Position?, regexStr: String): List<NodeVBean> {

    val r = try {
        Regex(regexStr)
    } catch (e: PatternSyntaxException) {
        ctx.messageCollector.reportError(
            e.message ?: "Invalid regex",
            positionInfo
        )
    }

    val matching =
        ctx.jjtxOptsModel
            .typeHierarchy
            .descendantsOrSelf()
            .filter { r.matches(it.name) }
            .toList()

    if (matching.isEmpty()) {
        ctx.messageCollector.report(
            "Regex pattern matches no nodes",
            MessageCategory.UNMATCHED_HIERARCHY_REGEX,
            positionInfo
        )
    }

    return matching
}


data class NodeGenerationBean(
    var formatter: String?,
    var genClassName: String?,
    var template: String?,
    var templateFile: String?,
    var context: Map<String, Any>?
) {

    fun promote(ctx: JjtxContext, positionInfo: Position?, nodeBeans: List<NodeVBean>): NodeGenerationScheme? {


        val t = if (templateFile == null && template == null) {
            ctx.messageCollector.reportNonFatal(
                "Node generation spec must mention either 'templateFile' or 'template'",
                positionInfo
            )
            return null
        } else if (template != null) {
            StringSource.Str(template!!)
        } else {
            StringSource.File(templateFile!!)
        }

        val formatterChoice = FormatterRegistry.select(formatter)

        return NodeGenerationScheme(
            nodeBeans,
            genClassName,
            t,
            context ?: emptyMap(),
            formatterChoice
        )

    }

}

data class NodeGenerationScheme(
    val nodeBeans: List<NodeVBean>,
    val genClassTemplate: String?,
    val template: StringSource,
    val context: Map<String, Any>,
    val formatter: SourceFormatter?
) {

    fun toFileGenTasks(): List<FileGenTask> =
        nodeBeans.map {
            FileGenTask(
                template = template,
                context = mapOf("node" to it).plus(context),
                formatter = formatter,
                genFqcn = genClassTemplate ?: it.klass.qualifiedName
            )
        }

}


data class GrammarGenerationScheme(
    val templates: List<NodeGenerationScheme>,
    val id: String

)
