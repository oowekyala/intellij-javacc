package com.github.oowekyala.ijcc.ide.refs

import com.intellij.openapi.project.Project

/**
 * @author Clément Fournier
 */
open class JccRefVariantService {


    open fun nonterminalRefVariants(ref: JccNonTerminalReference): Array<Any> = emptyArray()
    open fun terminalVariants(ref: JccTerminalReference): Array<Any> = emptyArray()
    open fun lexicalStateVariants(ref: JccLexicalStateReference): Array<Any> = emptyArray()
    open fun stringLiteralVariants(ref: JccBnfStringLiteralReference): Array<Any> = emptyArray()
    open fun jjtreeNodeVariants(ref: JjtNodePolyReference): Array<Any> = emptyArray()


    companion object {
        @JvmStatic
        fun getInstance(project: Project): JccRefVariantService =
            project.getService(JccRefVariantService::class.java) ?: NO_VARIANTS

        private val NO_VARIANTS = JccRefVariantService()
    }

}
