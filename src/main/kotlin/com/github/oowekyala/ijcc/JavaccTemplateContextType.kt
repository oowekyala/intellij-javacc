package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

/**
 * Context for live templates.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccTemplateContextBase(
    displayName: String
) : TemplateContextType(displayName) {

    override fun isInContext(ctx: TemplateActionContext): Boolean {
        return ctx.file is JccFile
                && isInJccContext(ctx.file as JccFile, ctx)
    }

    abstract fun isInJccContext(file: JccFile, ctx: TemplateActionContext): Boolean

    companion object {

        class Generic : JccTemplateContextBase(JavaccLanguage.displayName) {

            override fun isInJccContext(file: JccFile, ctx: TemplateActionContext): Boolean = true
        }

        class OptionsCtx : JccTemplateContextBase("Options declaration") {


            override fun isInJccContext(file: JccFile, ctx: TemplateActionContext): Boolean {

                val pdeclOffset = file.parserDeclaration?.textOffset ?: Int.MAX_VALUE

                return file.options == null && ctx.endOffset < pdeclOffset
            }
        }

        class ParserDeclCtx : JccTemplateContextBase("Parser declaration") {


            override fun isInJccContext(file: JccFile, ctx: TemplateActionContext): Boolean {

                val optionsEnd = file.options?.let { it.textOffset + it.textLength } ?: 0

                return file.parserDeclaration == null && ctx.startOffset > optionsEnd
            }
        }

        class ProductionCtx :
            JccTemplateContextBase("Production declarations") {

            override fun isInJccContext(file: JccFile, ctx: TemplateActionContext): Boolean {
                val parserEnd = file.parserDeclaration?.let { it.textOffset + it.textLength } ?: 0

                return ctx.startOffset > parserEnd
            }
        }
    }
}

