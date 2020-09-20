package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.psi.PsiElement
import com.intellij.usages.UsageTarget
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProviderEx

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccUsageTypeProvider : UsageTypeProviderEx {

    override fun getUsageType(element: PsiElement, targets: Array<out UsageTarget>): UsageType? = when {
        element is JccNonTerminalExpansionUnit                     -> JccUsageTypes.NONTERMINAL_REFERENCE
        element is JccLiteralRegexUnit                             -> JccUsageTypes.IMPLICIT_STRING_TOKEN_REFERENCE
        element is JccTokenReferenceRegexUnit                      -> JccUsageTypes.EXPLICIT_TOKEN_REFERENCE
        element is JccIdentifier && element.isJjtreeNodeIdentifier -> JccUsageTypes.JJTREE_NODE_PARTIAL_DECLARATION
        element is JjtNodeClassOwner                               -> JccUsageTypes.JJTREE_NODE_PARTIAL_DECLARATION
        else                                                       -> null
    }


    override fun getUsageType(element: PsiElement): UsageType? =
        getUsageType(element, UsageTarget.EMPTY_ARRAY)

    object JccUsageTypes {

        val NONTERMINAL_REFERENCE = UsageType { "Non-terminal reference" }
        val JJTREE_NODE_PARTIAL_DECLARATION = UsageType { "JJTree node partial declaration" }
        val IMPLICIT_STRING_TOKEN_REFERENCE = UsageType { "String token implicit reference" }
        val EXPLICIT_TOKEN_REFERENCE = UsageType { "Token explicit reference" }

    }

}
