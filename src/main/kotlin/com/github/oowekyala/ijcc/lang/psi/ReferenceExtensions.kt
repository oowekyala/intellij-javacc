package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.ide.refs.JccBnfStringLiteralReference
import com.github.oowekyala.ijcc.ide.refs.JccNonTerminalReference
import com.github.oowekyala.ijcc.ide.refs.JccTerminalReference
import com.github.oowekyala.ijcc.ide.refs.JjtNodePolyReference
import com.github.oowekyala.ijcc.lang.model.ExplicitToken
import com.github.oowekyala.ijcc.lang.model.SyntheticToken
import com.github.oowekyala.ijcc.lang.model.Token

/**
 * @author Clément Fournier
 * @since 1.0
 */

// those are mostly implemented in JccPsiElementImpl

/**
 * Returns a reference able to resolve usages of this string in BNF.
 * This property is null if this literal regex unit doesn't identify
 * a string token (as defined by [JccLiteralRegexUnit.isStringToken]).
 */
val JccLiteralRegexUnit.typedReference: JccBnfStringLiteralReference?
    get() = reference as JccBnfStringLiteralReference?


val JccLiteralRegularExpression.typedReference: JccBnfStringLiteralReference
    get() = unit.typedReference!!

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
    get() = when (val regex = regularExpression) {
        is JccRefRegularExpression -> regex.typedReference.resolveToken()
        else                       -> when (val unit = regex.asSingleLiteral(followReferences = false)) {
            null -> SyntheticToken(this) // everything else is synthesized
            else -> unit.typedReference!!.resolveToken(exact = true)
        }
    }

val JccRegularExpressionOwner.definedToken: Token
    get() = when (this) {
        is JccRegexSpec          -> ExplicitToken(this)
        is JccRegexExpansionUnit -> SyntheticToken(this)
        else                     -> throw IllegalStateException(this.toString())
    }
/**
 * Null if [JjtNodeClassOwner.isVoid], not null otherwise.
 * This is not yielded by PsiElement.getReference because it breaks
 * find usages.
 */
val JjtNodeClassOwner.typedReference: JjtNodePolyReference?
    get() = if (isNotVoid) JjtNodePolyReference(this) else null
