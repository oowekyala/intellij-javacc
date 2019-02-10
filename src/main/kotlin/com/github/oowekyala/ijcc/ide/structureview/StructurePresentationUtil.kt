package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.github.oowekyala.ijcc.util.plusAssign
import com.intellij.ide.projectView.PresentationData
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.PsiFormatUtilBase
import com.intellij.ui.RowIcon
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.UIUtil
import groovyjarjarantlr.build.ANTLR.root
import javax.swing.Icon


fun JccPsiElement.getPresentationForStructure(): ItemPresentation =
    PresentationData(getPresentableText(), getLocationString(), getPresentationIcon(), null)

/**
 * For the structure view.
 */
fun JccPsiElement.getPresentableText(): String {
    return when (this) {
        is JccOptionSection         -> "Options" // TODO add parser class name
        is JccOptionBinding         -> "$name = $stringValue"

        is JccParserDeclaration     -> "class ${grammarOptions.parserSimpleName}"
        is JccTokenManagerDecls     -> "TOKEN_MGR_DECLS"

        is JccRegexProduction       -> regexKind.text

        is JccRegexSpec             -> regularExpression.getPresentableText()
        is JccRegexExpansionUnit    -> regularExpression.getPresentableText()
        is JccRegularExpression     -> getPresentableText()
        is JccNonTerminalProduction -> getPresentableText()

        is JccFile                  -> name
        else                        -> "" + this
    }
}


private fun JccRegularExpression.getPresentableText(): String {
    val builder = StringBuilder()

    builder.append('<')

    if (this is JccNamedRegularExpression) {
        builder.append(name).append(" : ")
    }

    getRootRegexElement(followReferences = false)?.getPresentableText(builder)

    builder.append('>')
    return builder.toString()
}

fun JccRegexElement.getPresentableText(): String =
    StringBuilder().also { getPresentableText(it) }.toString()

private fun JccRegexElement.getPresentableText(builder: StringBuilder) {
    when (this) {
        is JccLiteralRegexUnit,
        is JccTokenReferenceRegexUnit                     -> builder += this.text
        is JccRegexAlternativeElt, is JccRegexSequenceElt -> {
            val (seq, delim) = when (this) {
                is JccRegexSequenceElt    -> Pair(this.regexUnitList, " ")
                is JccRegexAlternativeElt -> Pair(this.regexElementList, " | ")
                else                      -> throw IllegalStateException(root.toString())
            }

            seq.asSequence()
                .takeWhile { it is JccLiteralRegexUnit || it is JccTokenReferenceRegexUnit }
                .map { it.text }
                .toList()
                .ifEmpty { listOf("...") }
                .joinTo(builder, separator = delim, limit = 2)
        }

        else                                              -> builder += "..."
    }
}

private fun JccNonTerminalProduction.getPresentableText(): String {
    val header = header

    val psiMethod = JccElementFactory.createJavaMethodForNonterminal(
        header.project,
        header
    )

    val dumb = DumbService.isDumb(psiMethod.project)
    val method = PsiFormatUtil.formatMethod(
        psiMethod,
        PsiSubstitutor.EMPTY,
        PsiFormatUtilBase.SHOW_NAME or PsiFormatUtilBase.TYPE_AFTER or PsiFormatUtilBase.SHOW_PARAMETERS or if (dumb) 0 else PsiFormatUtilBase.SHOW_TYPE,
        if (dumb) PsiFormatUtilBase.SHOW_NAME else PsiFormatUtilBase.SHOW_TYPE
    )
    return StringUtil.replace(StringUtil.replace(method, ":void", ""), ":", ": ")
}


fun JccPsiElement.getLocationString(): String? {
    when (this) {
        is JccRegexSpec       -> {
            val outboundState = lexicalStateTransition
            if (outboundState != null) {
                return "${UIUtil.rightArrow()} ${outboundState.name}"
            }
        }
        is JccRegexProduction -> {
            val states = lexicalStateList
            if (states != null) {
                val identList = states.identifierList

                return if (identList.isEmpty()) {
                    "<*>"
                } else {
                    identList.joinToString(separator = ", ", prefix = "<", postfix = ">") { it.name }
                }
            }
        }
    }
    return null
}

fun JccPsiElement.getPresentationIcon(): Icon? = when (this) {
    is JccOptionSection      -> JavaccIcons.JAVACC_OPTION
    is JccOptionBinding      -> JavaccIcons.JAVACC_OPTION

    is JccTokenManagerDecls  -> JavaccIcons.TOKEN_MGR_DECLS
    is JccParserDeclaration  -> JavaccIcons.PARSER_DECLARATION

    is JccRegexProduction    -> JavaccIcons.TOKEN
    is JccRegexSpec          -> JavaccIcons.TOKEN.append(visibilityIcon(regularExpression))
    is JccRegexExpansionUnit -> JavaccIcons.TOKEN.append(visibilityIcon(regularExpression))

    is JccBnfProduction      -> JavaccIcons.BNF_PRODUCTION.append(visibilityIcon(this))
    is JccJavacodeProduction -> JavaccIcons.JAVACODE_PRODUCTION.append(visibilityIcon(this))
    else                     -> getIcon(0) // this isn't implemented by our classes
}


private fun visibilityIcon(prod: JccRegularExpression): Icon {
    return when {
        prod.isPrivate -> PlatformIcons.PRIVATE_ICON
        else           -> PlatformIcons.PUBLIC_ICON
    }
}

private fun visibilityIcon(prod: JccNonTerminalProduction): Icon {
    val modifier = prod.header.javaAccessModifier.text.trim()
    return when (modifier) {
        ""                    -> PlatformIcons.PACKAGE_LOCAL_ICON
        PsiModifier.PUBLIC    -> PlatformIcons.PUBLIC_ICON
        PsiModifier.PRIVATE   -> PlatformIcons.PRIVATE_ICON
        PsiModifier.PROTECTED -> PlatformIcons.PROTECTED_ICON
        else                  -> throw IllegalArgumentException("unknown modifier")
    }
}

private fun Icon.append(other: Icon): RowIcon {
    val row = RowIcon(2)
    row.setIcon(this, 0)
    row.setIcon(other, 1)
    return row
}

