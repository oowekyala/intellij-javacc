package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.codeInsight.template.EverywhereContextType
import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

/**
 * Context for live templates.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccTemplateContextBase(
    id: String,
    displayName: String,
    baseContextType: Class<out TemplateContextType>
) : TemplateContextType("JAVACC_$id", displayName, baseContextType) {

    override fun isInContext(ctx: TemplateActionContext): Boolean {
        return ctx.file is JccFile
                && isInJccContext(ctx.file as JccFile, ctx)
    }

    abstract fun isInJccContext(file: JccFile, ctx: TemplateActionContext): Boolean

    companion object {

        class Generic : JccTemplateContextBase("CODE", JavaccLanguage.displayName, EverywhereContextType::class.java) {

            override fun isInJccContext(file: JccFile, ctx: TemplateActionContext): Boolean = true
        }

        class OptionsCtx : JccTemplateContextBase("OPTIONS", "Options declaration", Generic::class.java) {


            override fun isInJccContext(file: JccFile, ctx: TemplateActionContext): Boolean {

                val pdeclOffset = file.parserDeclaration?.textOffset ?: Int.MAX_VALUE

                return file.options == null && ctx.endOffset < pdeclOffset
            }
        }

        class ParserDeclCtx : JccTemplateContextBase("PARSER_DECL", "Parser declaration", Generic::class.java) {


            override fun isInJccContext(file: JccFile, ctx: TemplateActionContext): Boolean {

                val optionsEnd = file.options?.let { it.textOffset + it.textLength } ?: 0

                return file.parserDeclaration == null && ctx.startOffset > optionsEnd
            }
        }

        class ProductionCtx :
            JccTemplateContextBase("PRODUCTION_DECLS", "Production declarations", Generic::class.java) {

            override fun isInJccContext(file: JccFile, ctx: TemplateActionContext): Boolean {
                val parserEnd = file.parserDeclaration?.let { it.textOffset + it.textLength } ?: 0

                return ctx.startOffset > parserEnd
            }
        }
    }
}

