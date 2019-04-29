package com.github.oowekyala.ijcc.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.jjtx.ErrorCollector
import com.github.oowekyala.ijcc.jjtx.JjtxRunContext


fun TypeHierarchyTree.expandAllNames(grammarNodeNames: Set<String>,
                                     ctx: JjtxRunContext): TypeHierarchyTree =
    resolveAgainst(grammarNodeNames, ctx).first()

/**
 * Unescaped the node shorthands in the type hierarchy and produces a new
 * root.
 *
 * Shorthands are
 *
 * "regex(something)" -> match all nodes with regex, can only be a leaf pattern, then expanded to package + prefix + name
 * "Something"        -> expanded to eg package.ASTSomething
 * "%Something"       -> expanded to package.Something
 * "foo.Something"    -> exactly foo.Something
 *
 */
private fun TypeHierarchyTree.resolveAgainst(grammarNodeNames: Set<String>,
                                             ctx: JjtxRunContext): List<TypeHierarchyTree> {


    RegexPattern.matchEntire(nodeName)?.groups?.get(1)?.run {
        return resolveRegex(grammarNodeNames, Regex(value), ctx) // TODO invalid regex?
    }

    val (qname, prodName, spec) = when {
        nodeName[0] == '%'              -> {
            val short = nodeName.substring(1)
            Triple(
                ctx.jjtxOptsModel.nodePackage + "." + short,
                short,
                Specificity.QUOTED
            )
        }
        nodeName.matches(Regex("\\w+")) ->
            Triple(
                ctx.jjtxOptsModel.nodePackage + "." + ctx.jjtxOptsModel.nodePrefix + nodeName,
                nodeName,
                Specificity.RESOLVED
            )
        else                            ->
            Triple(nodeName, nodeName, Specificity.QNAME)
    }

    if (prodName !in grammarNodeNames) {
        ctx.errorCollector.handleError(
            qname,
            ErrorCollector.Category.EXACT_NODE_NOT_IN_GRAMMAR,
            null,
            positionInfo
        )
    }

    return listOf(
        TypeHierarchyTree(
            qname,
            positionInfo,
            children.flatMap { it.resolveAgainst(grammarNodeNames, ctx) },
            spec
        )
    )
}

private fun TypeHierarchyTree.resolveRegex(grammarNodeNames: Set<String>,
                                           extractedRegex: Regex,
                                           ctx: JjtxRunContext): List<TypeHierarchyTree> {


    val matching = grammarNodeNames.filter { extractedRegex.matches(it) }


    if (children.isNotEmpty()) {
        val override = if (matching.size == 1) null else ErrorCollector.Severity.FAIL
        ctx.errorCollector.handleError(
            extractedRegex.pattern,
            ErrorCollector.Category.REGEX_SHOULD_BE_LEAF,
            override,
            positionInfo
        )
        if (override == ErrorCollector.Severity.FAIL) {
            return emptyList()
        }
    }


    if (matching.isEmpty()) {
        ctx.errorCollector.handleError(
            extractedRegex.pattern,
            ErrorCollector.Category.UNMATCHED_HIERARCHY_REGEX,
            null,
            positionInfo
        )
    }
    return matching.map {
        TypeHierarchyTree(
            ctx.jjtxOptsModel.nodePackage + "." + ctx.jjtxOptsModel.nodePrefix + it,
            positionInfo,
            emptyList(),
            Specificity.REGEX
        )
    }

}


enum class Specificity {
    UNKNOWN,
    REGEX,
    RESOLVED,
    QUOTED,
    QNAME,
    ROOT,
}

private val RegexPattern = Regex("^r:(.*)")
