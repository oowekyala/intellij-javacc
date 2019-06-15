package com.github.oowekyala.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.lang.model.addNodePackage
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.reporting.MessageCategory
import com.github.oowekyala.jjtx.reporting.report
import com.github.oowekyala.jjtx.reporting.reportFatal
import java.util.regex.PatternSyntaxException


internal fun TypeHierarchyTree.expandAllNames(grammarNodeNames: Set<String>,
                                              ctx: JjtxContext): TypeHierarchyTree =
    resolveAgainst(grammarNodeNames, ctx).first()

/**
 * Unescaped the node shorthands in the type hierarchy and produces a new
 * root.
 *
 * Shorthands are
 *
 * "r:something"      -> match all nodes with regex, can only be a leaf pattern, then expanded to package + prefix + name
 * "Something"        -> expanded to eg package.ASTSomething
 * "%Something"       -> expanded to package.Something
 * "foo.Something"    -> exactly foo.Something
 *
 */
private fun TypeHierarchyTree.resolveAgainst(grammarNodeNames: Set<String>,
                                             ctx: JjtxContext): List<TypeHierarchyTree> {


    RegexPattern.matchEntire(nodeName)?.groups?.get(1)?.run {

        val r = try {
            Regex(value)
        } catch (e: PatternSyntaxException) {
            ctx.messageCollector.reportFatal(
                e.message ?: "Invalid regex",
                positionInfo
            )
        }

        return resolveRegex(grammarNodeNames, r, ctx)
    }

    val (qname, prodName, spec) = when {
        nodeName[0] == '%'              -> {
            val short = nodeName.substring(1)
            Triple(
                ctx.jjtxOptsModel.addNodePackage(short),
                short,
                Specificity.QUOTED
            )
        }
        nodeName.matches(Regex("\\w+")) ->
            Triple(
                ctx.jjtxOptsModel.addNodePackage(ctx.jjtxOptsModel.nodePrefix + nodeName),
                nodeName,
                Specificity.RESOLVED
            )
        else                            ->
            Triple(nodeName, nodeName, Specificity.QNAME)
    }


    val isExternal = prodName !in grammarNodeNames
    if (isExternal) {
        ctx.messageCollector.report(
            "The node $qname is not in the grammar (can be generated anyway)",
            MessageCategory.EXACT_NODE_NOT_IN_GRAMMAR,
            positionInfo
        )
    }

    return listOf(
        TypeHierarchyTree(
            nodeName = qname,
            positionInfo = positionInfo,
            children = children.flatMap { it.resolveAgainst(grammarNodeNames, ctx) },
            isExternal = isExternal,
            specificity = if (parent == null) Specificity.ROOT else spec
        )
    )
}

private fun TypeHierarchyTree.resolveRegex(grammarNodeNames: Set<String>,
                                           extractedRegex: Regex,
                                           ctx: JjtxContext): List<TypeHierarchyTree> {


    val matching = grammarNodeNames.filter { extractedRegex.matches(it) }


    if (children.isNotEmpty()) {
        if (matching.size == 1) {
            ctx.messageCollector.report(
                "Regex patterns should only be used as leaves, this pattern matches ${matching.size} nodes",
                MessageCategory.REGEX_SHOULD_BE_LEAF,
                positionInfo
            )
        } else {
            ctx.messageCollector.reportFatal(
                "Regex patterns should only be used as leaves, this pattern matches ${matching.size} nodes",
                positionInfo
            )
        }
    }


    if (matching.isEmpty()) {
        ctx.messageCollector.report(
            "Regex pattern matches no nodes",
            MessageCategory.UNMATCHED_HIERARCHY_REGEX,
            positionInfo
        )
    }
    return matching.map {
        TypeHierarchyTree(
            nodeName = ctx.jjtxOptsModel.addNodePackage(ctx.jjtxOptsModel.nodePrefix + it),
            positionInfo = positionInfo,
            children = emptyList(),
            specificity = Specificity.REGEX,
            isExternal = false
        )
    }

}


internal enum class Specificity {
    UNKNOWN,
    REGEX,
    RESOLVED,
    QUOTED,
    QNAME,
    ROOT,
}

private val RegexPattern = Regex("^r:(.*)")
