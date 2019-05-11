package com.github.oowekyala.jjtx.jjtree

import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner

interface JjtxBuilderStrategy {

    fun makeNodeVar(owner: JjtNodeClassOwner, scopingDepth: Int): NodeVar?

    fun createNode(nodeVar: NodeVar): String

    fun openNodeHook(nodeVar: NodeVar): String?

    fun closeNodeHook(nodeVar: NodeVar): String?

    fun openNodeScope(nodeVar: NodeVar): String

    fun closeNodeScope(nodeVar: NodeVar): String

    fun clearNodeScope(nodeVar: NodeVar): String

    fun setFirstToken(nodeVar: NodeVar): String?

    fun setLastToken(nodeVar: NodeVar): String?

    fun popNode(nodeVar: NodeVar): String


}


/**
 * Imitates JJTree output.
 */
class VanillaJjtreeBuilder(private val grammarOptions: IGrammarOptions) : JjtxBuilderStrategy {

    private val bindings = grammarOptions.inlineBindings

    private val nodeFactory = bindings.jjtNodeFactory
    private val usesParser = bindings.jjtNodeConstructionUsesParser

    private val NodeVar.nodeId
        get() = "JJT" + nodeName.toUpperCase().replace('.', '_')

    override fun makeNodeVar(owner: JjtNodeClassOwner, scopingDepth: Int): NodeVar? =
        if (owner.isVoid) null
        else NodeVar(
            owner = owner,
            varName = buildVar("n", scopingDepth),
            closedVar = buildVar("c", scopingDepth),
            exceptionVar = buildVar("e", scopingDepth),
            nodeName = owner.nodeRawName!!,
            nodeQname = owner.nodeQualifiedName!!,
            nodeRefType =
            bindings.jjtNodeClass.takeIf { it.isNotEmpty() && !bindings.jjtMulti } ?: owner.nodeSimpleName!!
        )

    private fun buildVar(id: String, scopeDepth: Int): String {
        val s = "000$scopeDepth"
        return "jjt" + id + s.substring(s.length - 3, s.length)
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

    override fun openNodeHook(nodeVar: NodeVar): String? =
        if (bindings.jjtCustomNodeHooks) "jjtOpenNodeScope(${nodeVar.varName});" else null

    override fun closeNodeHook(nodeVar: NodeVar): String? =
        if (bindings.jjtCustomNodeHooks)
            "if (jjtree.nodeCreated()) jjtCloseNodeScope(${nodeVar.varName});"
        else null

    override fun setFirstToken(nodeVar: NodeVar): String? =
        if (grammarOptions.isTrackTokens) "${nodeVar.varName}.jjtSetFirstToken(getToken(1));" else null

    override fun setLastToken(nodeVar: NodeVar): String? =
        if (grammarOptions.isTrackTokens) "${nodeVar.varName}.jjtSetLastToken(getToken(0));" else null

    override fun openNodeScope(nodeVar: NodeVar): String = "jjtree.openNodeScope(${nodeVar.varName});"

    override fun clearNodeScope(nodeVar: NodeVar): String = "jjtree.clearNodeScope(${nodeVar.varName});"

    override fun closeNodeScope(nodeVar: NodeVar): String {
        val d = nodeVar.owner.jjtreeNodeDescriptor?.descriptorExpr
        val n = nodeVar.varName
        return when {
            d == null        -> "jjtree.closeNodeScope($n, true);"
            d.isGtExpression -> "jjtree.closeNodeScope($n, jjtree.nodeArity() > ${d.text});"
            else             -> "jjtree.closeNodeScope($n, ${d.text});"
        }
    }

    override fun popNode(nodeVar: NodeVar): String = "jjtree.popNode();"
}
