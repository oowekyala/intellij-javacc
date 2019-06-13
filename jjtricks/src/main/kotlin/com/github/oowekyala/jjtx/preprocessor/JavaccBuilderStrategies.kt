package com.github.oowekyala.jjtx.preprocessor

import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.ijcc.lang.model.parserQualifiedName
import com.github.oowekyala.ijcc.lang.model.parserSimpleName
import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.expressionText
import com.github.oowekyala.jjtx.JjtxContext
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

    fun setFirstToken(nodeVar: NodeVar): String?

    fun setLastToken(nodeVar: NodeVar): String?

    fun popNode(nodeVar: NodeVar): String

    fun escapeJjtThis(nodeVar: NodeVar, expression: String): String

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

    private val nodeFactory = bindings.jjtNodeFactory
    private val usesParser = bindings.jjtNodeConstructionUsesParser


    private val myImports = mutableSetOf<String>().also {
        if (grammarOptions.nodePackage.isNotEmpty()) {
            it += grammarOptions.nodePackage + ".*"
        }
    }

    private val NodeVar.nodeId
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

        val args = "(${if (usesParser) "this, " else ""}${nodeVar.nodeId})"

        return when {
            nodeFactory == "*"       -> "($nc) $nc.jjtCreate$args"
            nodeFactory.isNotEmpty() -> "($nc) $nodeFactory.jjtCreate$args"
            else                     -> "new $nc$args"
        }
    }

    override fun parserImplements(): List<String> =
        if (compat.implementNodeConstants)
            listOf("${grammarOptions.parserSimpleName}TreeConstants")
        else emptyList()

    override fun parserImports(): List<String> =
        myImports.also {
            if (!compat.implementNodeConstants) {
                it += "static ${grammarOptions.parserQualifiedName}TreeConstants.*"
            }
        }.sorted()


    override fun parserDeclarations(): String = """
        protected final $parserStateSimpleName jjtree = new $parserStateSimpleName();

    """.trimIndent()

    override fun openNodeHook(nodeVar: NodeVar): String? =
        if (bindings.jjtCustomNodeHooks) "jjtOpenNodeScope(${nodeVar.varName});" else null

    override fun closeNodeHook(nodeVar: NodeVar): String? =
        if (bindings.jjtCustomNodeHooks)
            "if (jjtree.nodeCreated()) jjtCloseNodeScope(${nodeVar.varName});"
        else null

    override fun setFirstToken(nodeVar: NodeVar): String? =
        if (grammarOptions.isTrackTokens) "jjtree.getManipulator().setFirstToken(jjtree, ${nodeVar.varName}, getToken(1));" else null

    override fun setLastToken(nodeVar: NodeVar): String? =
        if (grammarOptions.isTrackTokens) "jjtree.getManipulator().setLastToken(jjtree, ${nodeVar.varName}, getToken(0));" else null

    override fun openNodeScope(nodeVar: NodeVar): String = "jjtree.openNodeScope(${nodeVar.varName});"

    override fun clearNodeScope(nodeVar: NodeVar): String = "jjtree.clearNodeScope(${nodeVar.varName});"

    override fun closeNodeScope(nodeVar: NodeVar): String {
        val d = nodeVar.owner.jjtreeNodeDescriptor?.descriptorExpr
        val n = nodeVar.varName
        return when {
            d == null -> "jjtree.closeNodeScope($n, true);"
            d.isGtExpression -> "jjtree.closeNodeScope($n, jjtree.nodeArity() > ${escapeJjtThis(nodeVar, d.expressionText)});"
            else -> "jjtree.closeNodeScope($n, ${escapeJjtThis(nodeVar, d.expressionText)});"
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
