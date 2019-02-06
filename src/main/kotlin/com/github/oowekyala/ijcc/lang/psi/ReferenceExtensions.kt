package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.ExplicitToken
import com.github.oowekyala.ijcc.insight.model.SyntheticToken
import com.github.oowekyala.ijcc.insight.model.Token
import com.github.oowekyala.ijcc.insight.refs.JccNonTerminalReference
import com.github.oowekyala.ijcc.insight.refs.JccStringTokenReference
import com.github.oowekyala.ijcc.insight.refs.JccTerminalReference
import com.github.oowekyala.ijcc.insight.refs.JjtNodePolyReference

/**
 * @author Clément Fournier
 * @since 1.0
 */


val JccLiteralRegexpUnit.typedReference: JccStringTokenReference
    get() = reference as JccStringTokenReference


val JccLiteralRegularExpression.typedReference: JccStringTokenReference
    get() = unit.typedReference

val JccTokenReferenceUnit.typedReference: JccTerminalReference
    get() = reference as JccTerminalReference

val JccRegularExpressionReference.typedReference: JccTerminalReference
    get() = unit.typedReference

val JccNonTerminalExpansionUnit.typedReference: JccNonTerminalReference
    get() = reference as JccNonTerminalReference


/**
 * Returns the token, synthetic or explicit, that is referenced by this expansion
 * unit. Is null if the regex is a token reference (eg <foo>) whose token couldn't
 * be resolved.
 */
val JccRegexpExpansionUnit.referencedToken: Token?
    get() {
        val regex = regularExpression

        return when (regex) {
            is JccRegularExpressionReference -> regex.typedReference.resolveToken()?.let { ExplicitToken(it) }
            is JccLiteralRegularExpression   -> regex.typedReference.resolveToken(exact = true)
            // everything else is synthesized
            else                             -> SyntheticToken(this)
        }
    }

/**
 * Null if [JccNodeClassOwner.isVoid], not null otherwise.
 * This is not yielded by PsiElement.getReference because it breaks
 * find usages.
 */
val JccNodeClassOwner.typedReference: JjtNodePolyReference?
    get() = if (isNotVoid) JjtNodePolyReference(this) else null