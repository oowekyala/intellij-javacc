package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.*

// TODO account for node scopes
fun JccParenthesizedExpansionUnit.isNecessary(skipUndocumentable: Boolean = false): Boolean {


    // ()    // necessary?
    val inside = expansion ?: return true // empty parentheses are an error, but we consider them necessary
    // void foo():{}{ (..) } // unnecessary
    val outside = parent as? JccExpansion ?: return false // toplevel parentheses are unnecessary


    return when {
        // (..)+  // necessary
        // (..)*  // necessary
        // (..)?  // necessary
        occurrenceIndicator != null                                       -> true

        // ("f")            // unnecessary
        // (foo())          // unnecessary
        // (a=foo())        // unnecessary
        // (a="f")          // unnecessary
        // (a=<REG>)        // unnecessary
        // (<REG>)          // unnecessary
        // (try{..}catch{}) // unnecessary
        // (["hello"])      // unnecessary
        // ((..)?)          // unnecessary
        inside is JccExpansionUnit                                        -> false // expansions units are indivisible

        // LOOKAHEAD( ("foo" | "bar") )   // unnecessary
        // LOOKAHEAD(1, ("foo" | "bar") ) // unnecessary
        outside is JccLocalLookahead                                      -> false // then it's top level of a semantic lookahead

        // ("foo" | "bar") "bzaz"   // necessary
        // ("foo" | "bar") | "bzaz" // unnecessary
        inside is JccExpansionAlternative                                 -> outside !is JccExpansionAlternative

        // ("foo" "bar")  {}                // unnecessary
        // ("foo" "bar")  (hello() | "f")   // unnecessary, necessary
        outside is JccExpansionSequence && inside is JccExpansionSequence -> false
        else                                                              -> true
    }
}
/* TESTS

void parens() :
{}
{

 ("f")+           // necessary
 ("f")*           // necessary
 ("f")?           // necessary

 ("f")            // unnecessary
 (foo())          // unnecessary
 (a=foo())        // unnecessary
 (a="f")          // unnecessary
 (a=<THIS>)        // unnecessary
 (<THIS>)          // unnecessary
 (try{""}catch{}) // unnecessary
 (["hello"])      // unnecessary
 (("")?)          // unnecessary

 LOOKAHEAD( ("foo" | "bar") )     // unnecessary
 LOOKAHEAD(1, ("foo" | "bar") )   // unnecessary


 ("foo" | "bar") "bzaz"           // necessary
 ("foo" | "bar") | "bzaz"         // unnecessary

 ("foo" "bar")  {}                // unnecessary
 ("foo" "bar")  (foo() | "f")   // unnecessary, necessary
}

 void foo():{}{ ("") }            // unnecessary


*/

fun JccExpansionUnit.isUndocumentable(): Boolean = when (this) {
    is JccParserActionsUnit -> true
    is JccLocalLookahead    -> true
    else                    -> false
}