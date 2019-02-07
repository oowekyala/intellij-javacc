package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.ide.refs.JccNonTerminalReference
import com.github.oowekyala.ijcc.ide.refs.JccStringTokenReference
import com.github.oowekyala.ijcc.ide.refs.JccTerminalReference
import com.github.oowekyala.ijcc.ide.refs.JjtNodePolyReference
import com.github.oowekyala.ijcc.lang.model.ExplicitToken
import com.github.oowekyala.ijcc.lang.model.SyntheticToken
import com.github.oowekyala.ijcc.lang.model.Token

/**
 * @author Clément Fournier
 * @since 1.0
 */


val JccLiteralRegexUnit.typedReference: JccStringTokenReference
    get() = reference as JccStringTokenReference


val JccLiteralRegularExpression.typedReference: JccStringTokenReference
    get() = unit.typedReference

val JccTokenReferenceRegexUnit.typedReference: JccTerminalReference
    get() = reference as JccTerminalReference

val JccRefRegularExpression.typedReference: JccTerminalReference
    get() = unit.typedReference

val JccNonTerminalExpansionUnit.typedReference: JccNonTerminalReference
    get() = reference as JccNonTerminalReference


/**
 * Returns the token, synthetic or explicit, that is referenced by this expansion
 * unit. Is null if the regex is a token reference (eg `<foo>`) whose token couldn't
 * be resolved.
 */
val JccRegexExpansionUnit.referencedToken: Token?
    get() {
        val regex = regularExpression

        return when (regex) {
            is JccRefRegularExpression     -> regex.typedReference.resolveToken()?.let { ExplicitToken(it) }
            is JccLiteralRegularExpression -> regex.typedReference.resolveToken(exact = true)
            // everything else is synthesized
            else                           -> SyntheticToken(this)
        }
    }

val JccRegexSpec.definedToken: Token
    get() = ExplicitToken(this)

/**
 * Null if [JccNodeClassOwner.isVoid], not null otherwise.
 * This is not yielded by PsiElement.getReference because it breaks
 * find usages.
 */
val JccNodeClassOwner.typedReference: JjtNodePolyReference?
    get() = if (isNotVoid) JjtNodePolyReference(this) else null