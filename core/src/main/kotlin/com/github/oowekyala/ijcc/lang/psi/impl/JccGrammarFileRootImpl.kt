package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.injection.JavaccLanguageInjector
import com.github.oowekyala.ijcc.lang.injection.LinearInjectedStructure
import com.github.oowekyala.ijcc.lang.psi.JccGrammarFileRoot
import com.github.oowekyala.ijcc.lang.psi.JccOptionSection
import com.github.oowekyala.ijcc.lang.psi.JccParserDeclaration
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.settings.InjectionSupportLevel.DISABLED
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccGrammarFileRootImpl(node: ASTNode) : JccPsiElementImpl(node), JccGrammarFileRoot {
    override val linearInjectedStructure: LinearInjectedStructure by lazy {
        if (pluginSettings.injectionSupportLevel == DISABLED)
            throw IllegalStateException("This method should not be called if Java injection is disabled")
        else
            JavaccLanguageInjector.getLinearStructureFor(this)
    }

    override val optionSection: JccOptionSection?
        get() = findChildByClass(JccOptionSection::class.java)

    override val parserDeclaration: JccParserDeclaration
        get() = findNotNullChildByClass(JccParserDeclaration::class.java)

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
