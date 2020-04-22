package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.model.parserSimpleName
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.util.plusAssign
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.projectView.PresentationData
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.PsiFormatUtilBase
import com.intellij.ui.RowIcon
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.UIUtil
import groovyjarjarantlr.build.ANTLR.root
import javax.swing.Icon


val JccPsiElement.presentationForStructure: ItemPresentation
    get() = PresentationData(presentableText, locationString, presentationIcon, null)

/**
 * For the structure view.
 */
val JccPsiElement.presentableText: String
    get() {
        return when (this) {
            is JccOptionSection         -> "Options" // TODO add parser class name
            is JccOptionBinding         -> "$name = $stringValue"

            is JccParserDeclaration     -> "class ${grammarOptions.parserSimpleName}"
            is JccTokenManagerDecls     -> "TOKEN_MGR_DECLS"

            is JccRegexProduction       -> regexKind.text
            is JccScopedExpansionUnit   -> jjtreeNodeDescriptor.presentableText

            is JccRegexSpec             -> regularExpression.presentableText
            is JccRegexExpansionUnit    -> regularExpression.presentableText
            is JccRegularExpression     -> presentableText
            is JccNonTerminalProduction -> presentableText

            is JccFile                  -> name
            is JccIdentifier            -> owner?.presentableText ?: name
            is JccJjtreeNodeDescriptor  -> "#" + namingLeaf.text

            else                        -> toString()
        }
    }


private val JccRegularExpression.presentableText: String
    get() {
        if (this is JccEofRegularExpression) return "<EOF>"

        val builder = StringBuilder()

        builder.append('<')

        if (this is JccNamedRegularExpression) {
            builder.append(name).append(" : ")
        }

        getRootRegexElement(followReferences = false)?.getPresentableText(builder)

        builder.append('>')
        return builder.toString()
    }

val JccRegexElement.presentableText: String
    get() = StringBuilder().also { getPresentableText(it) }.toString()

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

private val JccNonTerminalProduction.presentableText: String
    get() {
        val header = header

        val psiMethod = header.project.jccEltFactory.createJavaMethodForNonterminal(
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


fun JccElementFactory.createJavaMethodForNonterminal(header: JccJavaNonTerminalProductionHeader): PsiMethod {
    val text = """
            class Foo {
                ${header.toJavaMethodHeader()} {

                }
            }
        """.trimIndent()

    return PsiFileFactory.getInstance(project).createFileFromText("dummy.java", JavaFileType.INSTANCE, text)
        .findChildOfType(PsiMethod::class.java)!!
}


val JccPsiElement.locationString: String?
    get() = when (this) {
        is JccRegexSpec             ->
            lexicalStateTransition?.let {
                "${UIUtil.rightArrow()} ${it.name}"
            }
        is JccRegexProduction       ->
            lexicalStateList?.identifierList?.let { identList ->

                if (identList.isEmpty()) {
                    "<*>"
                } else {
                    identList.joinToString(separator = ", ", prefix = "<", postfix = ">") { it.name }
                }
            }
        is JccNonTerminalProduction ->
            jjtreeNodeDescriptor
                ?.takeUnless { it.name?.equals(this.name) == true }
                ?.presentableText
        else                        -> null
    }

val JccPsiElement.presentationIcon: Icon?
    get() = when (this) {
        is JccOptionSection       -> JccIcons.JAVACC_OPTION
        is JccOptionBinding       -> JccIcons.JAVACC_OPTION

        is JccTokenManagerDecls   -> JccIcons.TOKEN_MGR_DECLS
        is JccParserDeclaration   -> JccIcons.PARSER_DECLARATION

        is JccScopedExpansionUnit -> JccIcons.JJTREE_NODE

        is JccRegexProduction     -> JccIcons.TOKEN
        is JccRegexSpec           -> JccIcons.TOKEN.append(visibilityIcon(regularExpression))
        is JccRegexExpansionUnit  -> JccIcons.TOKEN.append(visibilityIcon(regularExpression))

        is JccBnfProduction       ->
            (if (isVoid) JccIcons.VOID_BNF_PRODUCTION else JccIcons.JJT_BNF_PRODUCTION)
                .append(visibilityIcon(this))
        is JccJavacodeProduction  ->
            (if (isVoid) JccIcons.VOID_JAVACODE_PRODUCTION else JccIcons.JJT_JAVACODE_PRODUCTION)
                .append(visibilityIcon(this))
        else                      -> getIcon(0) // this isn't implemented by our classes
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

