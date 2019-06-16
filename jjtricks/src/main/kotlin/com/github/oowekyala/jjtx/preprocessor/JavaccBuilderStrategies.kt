package com.github.oowekyala.jjtx.preprocessor

import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.expressionText
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.postprocessor.SpecialTemplate
import com.github.oowekyala.jjtx.reporting.MessageCategory
import com.github.oowekyala.jjtx.reporting.report

/**
 * Strategy responsible for the rendering of hooks and API-dependent stuff.
 * Also responsible for generating the relevant support files
 */
interface JjtxBuilderStrategy {

    fun makeNodeVar(owner: JjtNodeClassOwner, enclosing: NodeVar?): NodeVar?

    fun parserImplements(): List<String>

    fun parserImports(): List<String>

    fun parserDeclarations(): String

    fun createNode(nodeVar: NodeVar): String

    fun openNodeScope(nodeVar: NodeVar): String

    fun closeNodeScope(nodeVar: NodeVar): String

    fun clearNodeScope(nodeVar: NodeVar): String

    fun popNode(nodeVar: NodeVar): String

    fun escapeJjtThis(nodeVar: NodeVar, expression: String): String

    // FIXME unused
    fun validateSupportFiles(ctx: JjtxContext): Boolean

    // those are shitty bindings that we honor when the inline binding is there but
    // otherwise should be done through the manipulator

    fun openNodeHook(nodeVar: NodeVar): String?

    fun closeNodeHook(nodeVar: NodeVar): String?

}


/**
 * Imitates JJTree output.
 */
class VanillaJjtreeBuilder(private val grammarOptions: IGrammarOptions,
                           private val compat: JavaccGenOptions) : JjtxBuilderStrategy {

    private val bindings = grammarOptions.inlineBindings

    private val nodeFactory = SpecialTemplate.NODE_FACTORY.actualLocation(grammarOptions)
    private val nodeIds = SpecialTemplate.NODE_IDS.actualLocation(grammarOptions)
    private val myImports = mutableSetOf<String>().also {
        if (grammarOptions.nodePackage.isNotEmpty()) {
            it += grammarOptions.nodePackage + ".*"
        }
        it += nodeFactory.qualifiedName
        it += nodeIds.qualifiedName
        it += "static ${nodeIds.qualifiedName}.*"
    }

    private val NodeVar.nodeId
        // FIXME link to nodeIds special template
        get() = "JJT" + nodeName.toUpperCase().replace('.', '_')

    override fun makeNodeVar(owner: JjtNodeClassOwner, enclosing: NodeVar?): NodeVar? =
        if (owner.isVoid) null
        else {

            val (nVar, cVar, exVar) = varNames(owner, enclosing)

            NodeVar(
                owner = owner,
                enclosingVar = enclosing,
                varName = nVar,
                closedVar = cVar,
                exceptionVar = exVar,
                nodeName = owner.nodeRawName!!,
                nodeQname = owner.nodeQualifiedName!!,
                nodeRefType =
                bindings.jjtNodeClass.takeIf { it.isNotEmpty() && !bindings.jjtMulti } ?: owner.nodeSimpleName!!
            )
        }

    /**
     * Builds the variable names for the node var, closed var, exception var.
     */
    private fun varNames(owner: JjtNodeClassOwner, enclosing: NodeVar?): Triple<String, String, String> {
        val scopeDepth = if (enclosing != null) enclosing.scopeDepth + 1 else 0
        val nodeVarName = "${owner.nodeRawName!!}$scopeDepth".decapitalize().removeSuffix("0")

        return Triple(
            nodeVarName,
            "${nodeVarName}NeedsClose",
            "${nodeVarName}Exception"
        )

    }


    override fun createNode(nodeVar: NodeVar): String {
        val nc = nodeVar.nodeRefType

        val args = "(" + (if (grammarOptions.nodeTakesParserArg) "this, " else "") + nodeVar.nodeId + ")"

        return "($nc) ${nodeFactory.simpleName}.jjtCreate$args"
    }

    override fun parserImplements(): List<String> =
        compat.implementsList


    override fun parserImports(): List<String> = myImports.sorted()


    override fun parserDeclarations(): String = """
        protected final $parserStateSimpleName jjtree = new $parserStateSimpleName();

    """.trimIndent()

    override fun openNodeHook(nodeVar: NodeVar): String? =
        if (bindings.jjtCustomNodeHooks) "jjtOpenNodeScope(${nodeVar.varName});" else null

    override fun closeNodeHook(nodeVar: NodeVar): String? =
        if (bindings.jjtCustomNodeHooks)
            "if (jjtree.nodeCreated()) jjtCloseNodeScope(${nodeVar.varName});"
        else null


    override fun openNodeScope(nodeVar: NodeVar): String = "jjtree.openNodeScope(${nodeVar.varName}, getToken(1));"

    override fun clearNodeScope(nodeVar: NodeVar): String = "jjtree.clearNodeScope(${nodeVar.varName});"

    override fun closeNodeScope(nodeVar: NodeVar): String {
        val d = nodeVar.owner.jjtreeNodeDescriptor?.descriptorExpr
        val n = nodeVar.varName
        val prefix = "jjtree.closeNodeScope($n, getToken(0)"
        return when {
            d == null        -> "$prefix, true);"
            d.isGtExpression -> "$prefix, jjtree.nodeArity() > ${escapeJjtThis(nodeVar, d.expressionText)});"
            else             -> "$prefix, ${escapeJjtThis(nodeVar, d.expressionText)});"
        }
    }


    override fun escapeJjtThis(nodeVar: NodeVar, expression: String): String =
        jjtThisRegex.replace(expression, nodeVar.varName)

    override fun popNode(nodeVar: NodeVar): String = "jjtree.popNode();"

    private val parserStateSimpleName: String get() = compat.supportFiles.getValue("treeBuilder").genFqcn

    override fun validateSupportFiles(ctx: JjtxContext): Boolean {
        val support = ctx.jjtxOptsModel.javaccGen.supportFiles

        if ("treeBuilder" !in support) {
            ctx.messageCollector.report(
                "Javacc grammar generation needs a 'treeBuilder' file generation task!",
                MessageCategory.NON_FATAL
            )
            return false
        }

        return true
    }

}

private val jjtThisRegex = Regex("\\bjjtThis\\b")
