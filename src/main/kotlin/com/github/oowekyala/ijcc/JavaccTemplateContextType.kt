package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.codeInsight.template.EverywhereContextType
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile

/**
 * Context for live templates.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccTemplateContextBase(id: String,
                                      displayName: String,
                                      baseContextType: Class<out TemplateContextType>)
    : TemplateContextType("JAVACC_$id", displayName, baseContextType) {

    override fun isInContext(file: PsiFile, offset: Int): Boolean {

        return file is JccFile && isInContext(file, offset)

    }

    abstract fun isInContext(file: JccFile, offset: Int): Boolean

    companion object {

        class Generic : JccTemplateContextBase("CODE", JavaccLanguage.displayName, EverywhereContextType::class.java) {

            override fun isInContext(file: JccFile, offset: Int): Boolean = true
        }

        class OptionsCtx : JccTemplateContextBase("OPTIONS", "Options declaration", Generic::class.java) {


            override fun isInContext(file: JccFile, offset: Int): Boolean {

                val pdeclOffset = file.parserDeclaration?.textOffset ?: Int.MAX_VALUE

                return file.options == null && offset < pdeclOffset
            }
        }

        class ParserDeclCtx : JccTemplateContextBase("PARSER_DECL", "Parser declaration", Generic::class.java) {


            override fun isInContext(file: JccFile, offset: Int): Boolean {

                val optionsEnd = file.options?.let { it.textOffset + it.textLength } ?: 0

                return file.parserDeclaration == null && offset > optionsEnd
            }
        }

        class ProductionCtx :
            JccTemplateContextBase("PRODUCTION_DECLS", "Production declarations", Generic::class.java) {

            override fun isInContext(file: JccFile, offset: Int): Boolean {
                val parserEnd = file.parserDeclaration?.let { it.textOffset + it.textLength } ?: 0

                return offset > parserEnd
            }
        }
    }
}

