package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.util.position
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement

/**
 * Reports syntax errors, returns true if any are found.
 */
fun JccFile.reportSyntaxErrors(ctx: JjtxContext): Boolean {

    // TODO check errors in the java compilation unit

    val visitor = object : JccVisitor() {

        var invalidSyntax = false

        override fun visitElement(element: PsiElement) {
            element.acceptChildren(this)
        }

        override fun visitErrorElement(element: PsiErrorElement) {
            invalidSyntax = true
            ctx.messageCollector.reportNonFatal("Syntax error: ${element.errorDescription}", position = element.position())
        }

    }

    this.accept(visitor)

    return visitor.invalidSyntax
}
