package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.*


data class ParenthesesConfig(val keepAroundAssignment: Boolean = false,
                             val keepAroundLookahead: Boolean = false,
                             val keepBeforeParserActions: Boolean = false,
                             val keepUndocumented: Boolean)

fun JccParenthesizedExpansionUnit.isNecessary(config: ParenthesesConfig): Boolean {


    // ()    // necessary?
    val inside = expansion ?: return true // empty parentheses are an error, but we consider them necessary
    val outside = parent!!

    val keepLookahead = config.keepUndocumented && config.keepAroundLookahead
    val keepParserActions = config.keepUndocumented && config.keepBeforeParserActions
    val keepAssignment = config.keepUndocumented && config.keepAroundAssignment

    return when {
        // (..)+  // necessary
        // (..)*  // necessary
        // (..)?  // necessary
        occurrenceIndicator != null                                       -> true

        // void foo():{}{ (..) } // unnecessary
        outside !is JccExpansion                                          -> false // top level parens are unnecessary, unless there's an occurrence indicator

        // (a=foo())        // clarifying
        // (a="f")          // unnecessary
        inside is JccAssignedExpansionUnit                                -> keepAssignment

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

        // (LOOKAHEAD(2) "foo" | "foo" "bar") // clarifying
        // (LOOKAHEAD(2) "foo" "f" | "foo" "bar") // clarifying
        // ("foo" | "bar") "bzaz"   // necessary
        // ("foo" | "bar") | "bzaz" // unnecessary
        inside is JccExpansionAlternative                                 ->
            keepLookahead && inside.isLookahead() || outside !is JccExpansionAlternative


        // ("foo" "bar")  {}                // unnecessary
        // ("foo" "bar")  (hello() | "f")   // unnecessary, necessary
        outside is JccExpansionSequence && inside is JccExpansionSequence -> {
            val nextSibling = nextSiblingNoWhitespace
            when {
                // ("foo" "bar")  {foo();}            // clarifying
                nextSibling is JccParserActionsUnit -> keepParserActions
                // (LOOKAHEAD(2) "foo" "foo" "bar") // clarifying
                inside.isLookahead()                -> keepLookahead
                else                                -> false
            }
        }
        else                                                              -> true
    }
}

private fun JccExpansionSequence.isLookahead() = expansionUnitList[0] is JccLocalLookahead
private fun JccExpansionAlternative.isLookahead(): Boolean {
    val fst = expansionList[0]
    return fst is JccLocalLookahead || fst is JccExpansionSequence && fst.isLookahead()
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

 LOOKAHEAD( ("foo" | "bar") )     // unnecessary
 LOOKAHEAD(1, ("foo" | "bar") )   // unnecessary


 ("foo" | "bar") "bzaz"           // necessary
 (("foo" | "bar") | "bzaz")       // necessary, unnecessary

 ("foo" "bar")  {}                // unnecessary
 ("foo" "bar")  (foo() | "f")     // unnecessary, necessary
 ("(" Expr() ")")     #Node       // necessary
 ("(")     #Node                  // unnecessary
 (LOOKAHEAD(2) "foo" | "foo" "bar")     // clarifying
 (LOOKAHEAD(2) "foo" "f" | "foo" "bar") // necessary
 ((LOOKAHEAD(2) "foo" "f" | "foo" "bar") | "f") // clarifying

}

 void foo():{}{ ("") }            // unnecessary
 void bar():{}{ ("")? }           // necessary


*/