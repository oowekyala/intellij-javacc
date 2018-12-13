// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.injection.InjectedTreeBuilderVisitor
import com.github.oowekyala.ijcc.lang.injection.LinearInjectedStructure
import com.github.oowekyala.ijcc.lang.injection.TreeLineariserVisitor
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccGrammarFileRootImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccGrammarFileRoot {
    override val linearInjectedStructure: LinearInjectedStructure by lazy {
        InjectedTreeBuilderVisitor.getSubtreeFor(this).let {
            TreeLineariserVisitor().startOn(it)
        }
    }

    override val nonTerminalProductionList: List<JccNonTerminalProduction>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccNonTerminalProduction::class.java)

    override val optionSection: JccOptionSection?
        get() = findChildByClass(JccOptionSection::class.java)

    override val parserDeclaration: JccParserDeclaration
        get() = findNotNullChildByClass(JccParserDeclaration::class.java)

    override val regularExprProductionList: List<JccRegularExprProduction>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccRegularExprProduction::class.java)

    override val tokenManagerDeclsList: List<JccTokenManagerDecls>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccTokenManagerDecls::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitGrammarFileRoot(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
