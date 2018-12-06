package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.*


data class ParenthesesConfig(val keepAroundAssignment: Boolean = false,
                             val keepAroundLookahead: Boolean = false,
                             val keepBeforeParserActions: Boolean = false,
                             val keepUndocumented: Boolean)

fun JccParenthesizedExpansionUnit.isNecessary(config: ParenthesesConfig): Boolean {


    // ()    // necessary?
    val inside = expansion ?: return true // empty parentheses are an error, but we consider them necessary
    // void foo():{}{ (..) } // unnecessary
    val outside = parent as? JccExpansion ?: return false // toplevel parentheses are unnecessary


    return when {
        // (..)+  // necessary
        // (..)*  // necessary
        // (..)?  // necessary
        occurrenceIndicator != null                                       -> true

        // (a=foo())        // clarifying
        // (a="f")          // unnecessary
        inside is JccAssignedExpansionUnit                                -> config.keepAroundAssignment

        // ("f")            // unnecessary
        // (foo())          // unnecessary
        // (a=<REG>)        // unnecessary
        // (<REG>)          // unnecessary
        // (try{..}catch{}) // unnecessary
        // (["hello"])      // unnecessary
        // ((..)?)          // unnecessary
        // ("(") #Node      // unnecessary
        inside is JccExpansionUnit                                        -> false // expansions units are indivisible

        //  ("(" Expr() ")")     #Node    // necessary unless doc
        outside is JccScopedExpansionUnit                                 -> config.keepUndocumented
        // LOOKAHEAD( ("foo" | "bar") )   // unnecessary
        // LOOKAHEAD(1, ("foo" | "bar") ) // unnecessary
        outside is JccLocalLookahead                                      -> false // then it's top level of a semantic lookahead
        // a=("f")          // unnecessary
        // a=(h())          // unnecessary
        outside is JccAssignedExpansionUnit                               -> false

        // ("foo" | "bar") "bzaz"   // necessary
        // ("foo" | "bar") | "bzaz" // unnecessary
        inside is JccExpansionAlternative                                 -> outside !is JccExpansionAlternative

        // ("foo" "bar")  {}                // unnecessary
        // ("foo" "bar")  (hello() | "f")   // unnecessary, necessary
        outside is JccExpansionSequence && inside is JccExpansionSequence -> {
            val nextSibling = nextSiblingNoWhitespace
            when {
                // ("foo" "bar")  {foo();}            // clarifying
                nextSibling is JccParserActionsUnit              -> config.keepUndocumented && config.keepBeforeParserActions
                // (LOOKAHEAD(2) "foo" | "foo" "bar") // clarifying
                inside.expansionUnitList[0] is JccLocalLookahead -> config.keepUndocumented && config.keepAroundLookahead
                else                                             -> false
            }
        }
        else                                                              -> true
    }
}
/* TESTS



void parens() :
{}
{

 ("f")+                           // necessary
 ("f")*                           // necessary
 ("f")?                           // necessary

 ("f")                            // unnecessary
 (foo())                          // unnecessary
 (a=foo())                        // unnecessary
 (a="f")                          // unnecessary
 (a=<THIS>)                       // unnecessary
 (<THIS>)                         // unnecessary
 (try{""}catch(foo f){})          // unnecessary
 (["hello"])                      // unnecessary
 (("")?)                          // unnecessary
 a=("f")                          // unnecessary
 a=(h())                          // unnecessary

 LOOKAHEAD( ("foo" | "bar") )     // unnecessary
 LOOKAHEAD(1, ("foo" | "bar") )   // unnecessary


 ("foo" | "bar") "bzaz"           // necessary
 (("foo" | "bar") | "bzaz")       // necessary, unnecessary

 ("foo" "bar")  {}                // unnecessary
 ("foo" "bar")  (foo() | "f")     // unnecessary, necessary
 ("(" Expr() ")")     #Node       // necessary
 ("(")     #Node                  // unnecessary
 (LOOKAHEAD(2) "foo" | "foo" "bar") // clarifying
}

 void foo():{}{ ("") }            // unnecessary


*/