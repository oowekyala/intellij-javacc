package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil


/**
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccFoldingBuilder : CustomFoldingBuilder() {

    override fun buildLanguageFoldRegions(
        descriptors: MutableList<FoldingDescriptor>,
        root: PsiElement,
        document: Document,
        quick: Boolean
    ) {

        PsiTreeUtil.findChildrenOfAnyType(
            root,
            JccJavaBlock::class.java,
            JccLocalLookahead::class.java,
            JccRegularExprProduction::class.java,
            JccJavaccOptions::class.java,
            JccParserDeclaration::class.java,
            JccTokenManagerDecls::class.java,
            JccRegularExpressionReference::class.java
        ).forEach {
            val descriptor = when (it) {
                is JccJavaBlock                  -> getDescriptorForJavaBlock(it)
                is JccLocalLookahead             -> getDescriptorForLookahead(it)
                is JccRegularExprProduction      -> getDescriptorForRegexProduction(it)
                is JccJavaccOptions              -> getDescriptorForOptions(it)
                is JccParserDeclaration          -> getDescriptorForParserDecl(it)
                is JccTokenManagerDecls          -> getDescriptorForTokenMgrDecl(it)
                is JccRegularExpressionReference -> getDescriptorForRegexRef(it)
                else                             -> null
            }

            if (descriptor != null) descriptors.add(descriptor)

        }
    }

    private fun <T : Any> T?.onlyIf(predicate: (T) -> Boolean): T? =
        if (this == null || !predicate(this)) null
        else this

    private fun <T : Any, R> T?.map(f: (T) -> R?): R? =
        if (this == null) null
        else f(this)

    private fun getDescriptorForRegexRef(regexRef: JccRegularExpressionReference): FoldingDescriptor? =
        literalRegexpForRef(regexRef).map { FoldingDescriptor(regexRef, regexRef.textRange) }

    private fun literalRegexpForRef(regexRef: JccRegularExpressionReference): JccLiteralRegularExpression? {
        val decl = regexRef.reference.resolve()
        if (decl is JccIdentifier && decl.parent is JccNamedRegularExpression) {
            val parent = decl.parent as JccNamedRegularExpression
            return parent.complexRegexpChoices
                ?.complexRegexpSequenceList
                .onlyIf { it.size == 1 }
                ?.get(0)
                ?.complexRegexpUnitList
                .onlyIf { it.size == 1 }
                ?.get(0)
                .map { it.literalRegularExpression }
        }
        return null
    }

    private fun getDescriptorForTokenMgrDecl(decl: JccTokenManagerDecls): FoldingDescriptor? {
        return FoldingDescriptor(
            decl,
            decl.textRange
        )
    }

    private fun getDescriptorForParserDecl(decl: JccParserDeclaration): FoldingDescriptor? {
        return FoldingDescriptor(
            decl,
            decl.textRange
        )
    }

    private fun getDescriptorForRegexProduction(production: JccRegularExprProduction): FoldingDescriptor? {
        return FoldingDescriptor(
            production,
            production.textRange
        )
    }

    private fun getDescriptorForOptions(options: JccJavaccOptions): FoldingDescriptor? {
        return FoldingDescriptor(
            options,
            options.textRange
        )
    }

    private fun getDescriptorForLookahead(lookahead: JccLocalLookahead): FoldingDescriptor? {
        return FoldingDescriptor(
            lookahead,
            lookahead.textRange
        )
    }


    private fun getDescriptorForJavaBlock(javaBlock: JccJavaBlock): FoldingDescriptor? {
        if (javaBlock.parent !is JccJavacodeProduction && javaBlock.textLength > 2) { // not just "{}"
            var range = javaBlock.textRange
            if (javaBlock.prevSibling.node.elementType == TokenType.WHITE_SPACE) {
                range = range.union(javaBlock.prevSibling.textRange)
            }

            return FoldingDescriptor(
                javaBlock,
                range
            )
        }
        return null
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean = true

    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String {
        val psi = node.psi
        return when (psi) {
            is JccRegularExpressionReference -> literalRegexpForRef(psi)!!.text
            is JccParserDeclaration          -> "/PARSER DECLARATION/"
            is JccTokenManagerDecls          -> "/TOKEN MANAGER DECLARATIONS/"
            is JccRegularExprProduction      -> "${psi.regexprKind.text}: {..}"
            is JccJavaccOptions              -> "options {..}"
            is JccJavaBlock                  -> " {..}"
            is JccLocalLookahead             -> {
                if (psi.integerLiteral != null && psi.expansionChoices == null && psi.javaExpression == null) {
                    "LOOKAHEAD(${psi.integerLiteral!!.text})"
                } else "LOOKAHEAD(..)"
            }
            else                             -> throw UnsupportedOperationException("Unhandled case")
        }
    }
}
