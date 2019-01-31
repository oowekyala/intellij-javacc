package com.github.oowekyala.ijcc.lang.psi

/*

void lookaheads(): {}
{
    LOOKAHEAD(1, ID(), {getToken(1).kind != NATURAL})       // lexical, syntactic, semantic
    LOOKAHEAD({getToken(1).kind != NATURAL})                // semantic, not syntactic
    LOOKAHEAD(1)                                            // lexical
    LOOKAHEAD(TableAlias(), {getToken(1).kind != NATURAL})  // syntactic, semantic
    LOOKAHEAD(1, ("foo" | "bar") )                          // lexical, syntactic

    LOOKAHEAD({ doSth(); }, {getToken(1).kind != NATURAL})  // invalid parse
}
 */
val JccLocalLookahead.isLexical
    get() = integerLiteral != null

val JccLocalLookahead.isSyntactic
    get() = expansion != null

val JccLocalLookahead.isSemantic
    get() = javaExpression != null

val JccLocalLookahead.lexicalAmount: Int?
    get() = integerLiteral?.text?.toInt()
