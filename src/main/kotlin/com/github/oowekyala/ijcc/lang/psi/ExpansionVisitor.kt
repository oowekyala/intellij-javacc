package com.github.oowekyala.ijcc.lang.psi

/**
 * Visits the whole hierarchy of [JccExpansion]. An expression
 * of intention.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class ExpansionVisitor : JccVisitor() {

    override fun visitExpansion(o: JccExpansion) {
        super.visitExpansion(o)
    }

    override fun visitExpansionAlternative(o: JccExpansionAlternative) {
        super.visitExpansionAlternative(o)
    }

    override fun visitExpansionSequence(o: JccExpansionSequence) {
        super.visitExpansionSequence(o)
    }

    override fun visitExpansionUnit(o: JccExpansionUnit) {
        super.visitExpansionUnit(o)
    }


    override fun visitRegexpExpansionUnit(o: JccRegexpExpansionUnit) {
        super.visitRegexpExpansionUnit(o)
    }

    override fun visitAssignedExpansionUnit(o: JccAssignedExpansionUnit) {
        super.visitAssignedExpansionUnit(o)
    }

    override fun visitNonTerminalExpansionUnit(o: JccNonTerminalExpansionUnit) {
        super.visitNonTerminalExpansionUnit(o)
    }

    override fun visitOptionalExpansionUnit(o: JccOptionalExpansionUnit) {
        super.visitOptionalExpansionUnit(o)
    }

    override fun visitParenthesizedExpansionUnit(o: JccParenthesizedExpansionUnit) {
        super.visitParenthesizedExpansionUnit(o)
    }

    override fun visitScopedExpansionUnit(o: JccScopedExpansionUnit) {
        super.visitScopedExpansionUnit(o)
    }

    override fun visitTryCatchExpansionUnit(o: JccTryCatchExpansionUnit) {
        super.visitTryCatchExpansionUnit(o)
    }

    override fun visitParserActionsUnit(o: JccParserActionsUnit) {
        super.visitParserActionsUnit(o)
    }

    override fun visitLocalLookahead(o: JccLocalLookahead) {
        super.visitLocalLookahead(o)
    }

}