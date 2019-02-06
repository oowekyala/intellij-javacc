package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.*


// TODO check unnecessary parentheses in regular expressions

data class ParenthesesConfig(val keepAroundAssignment: Boolean = false,
                             val keepAroundLookahead: Boolean = false,
                             val keepBeforeParserActions: Boolean = false)

fun JccParenthesizedExpansionUnit.isUnnecessary(config: ParenthesesConfig): Boolean = !isNecessary(config)

fun JccParenthesizedExpansionUnit.isNecessary(config: ParenthesesConfig): Boolean {


    // ()    // necessary?
    val inside = expansion ?: return true // empty parentheses are an error, but we consider them necessary
    val outside = parent!!

    val keepLookahead = config.keepAroundLookahead
    val keepParserActions = config.keepBeforeParserActions
    val keepAssignment = config.keepAroundAssignment

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
        outside is JccScopedExpansionUnit                                 -> true

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
                keepParserActions && nextSibling is JccParserActionsUnit -> true
                // (LOOKAHEAD(2) "foo" "foo" "bar") // clarifying
                keepLookahead && inside.isLookahead()                    -> true
                else                                                     -> false
            }
        }

        else                                                              -> true
    }
}

private fun JccExpansionUnit.isDocumented() = when (this) {
    is JccParserActionsUnit -> false
    is JccLocalLookahead    -> false
    else                    -> true
}

private fun JccExpansionSequence.isLookahead() = expansionUnitList[0] is JccLocalLookahead
private fun JccExpansionAlternative.isLookahead(): Boolean {
    val fst = expansionList[0]
    return fst is JccLocalLookahead || fst is JccExpansionSequence && fst.isLookahead()
}
/* TESTS

void docParens1():{} // doc should be "a"
{
  ( "a" ) { foo(); }
}

void docParens2():{} // doc should be "a" \n| "b"
{
  ( "a" | "b" ) { foo(); }
}

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

/** specialized for the quickdoc maker */
fun JccParenthesizedExpansionUnit.docIsNecessary(): Boolean {


    // ()    // necessary?
    val inside = expansion ?: return true // empty parentheses are an error, but we consider them necessary
    val outside = parent!!

    return when {
        occurrenceIndicator != null                                             -> true

        outside !is JccExpansion                                                -> false // top level parens are unnecessary, unless there's an occurrence indicator

        inside is JccExpansionUnit                                              -> false // expansions units are indivisible

        //  ("(" Expr() ")")     #Node    // necessary unless doc
        outside is JccScopedExpansionUnit                                       -> false

        outside is JccExpansionSequence && inside is JccExpansionSequence       -> false
        outside is JccExpansionAlternative && inside is JccExpansionAlternative -> false
        outside is JccExpansionAlternative && inside is JccExpansionSequence    -> false

        outside is JccExpansionSequence && inside is JccExpansionAlternative    ->
            // parens are then needed, unless the rest of the sequence is only undocumented elements
            outside.expansionUnitList.filter { it !== this }.any { it.isDocumented() }


        else                                                                    -> true
    }
}