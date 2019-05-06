package com.github.oowekyala.ijcc.ide.refs

import com.intellij.openapi.components.ServiceManager

/**
 * @author Clément Fournier
 */
open class JccRefVariantService {


    open fun nonterminalRefVariants(ref: JccNonTerminalReference): Array<Any> = emptyArray()
    open fun terminalVariants(ref: JccTerminalReference): Array<Any> = emptyArray()
    open fun lexicalStateVariants(ref: JccLexicalStateReference): Array<Any> = emptyArray()
    open fun stringLiteralVariants(ref: JccBnfStringLiteralReference): Array<Any> = emptyArray()


    companion object {
        @JvmStatic
        fun getInstance(): JccRefVariantService =
            ServiceManager.getService(JccRefVariantService::class.java) ?: NO_VARIANTS

        private val NO_VARIANTS = JccRefVariantService()
    }

}
