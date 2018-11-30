# Things missing in the javacc grammar

* {min,max} quantifiers for regex (complex_regular_expression_unit)
* "try" expansion unit

# Things to do before release

++ Fix bugs with lookahead not expanding anymore
++ Rebuild the structure view
++ Make new icons
++ Add quick doc for options and productions
++ Highlight scope of jjtree decoration and parser action
++ AUTOCOMPLETE
* Group tokens by lexical state in structure view
++ BIG PERFORMANCE PROBLEM -> probably stop building lexical states
* Doc is not displayed on tokens in structure view
* Weird behaviour of CTRL-hover of production names -> the void keyword looks like it's the ident
* The ident also gives the type in the structure view...

* Make a good plugin description
* Make a readme
* Check with JB about Intellij copyright
* Find a license

## JavaCC errors

++ Duplicate string literals
++ Duplicate named token
* Token name "DIGITS" refers to a private (with a #) regular expression.

* Wrong option type

## Inspection ideas

++ Unnecessary #void annotation
++ Unnecessary inline regex (in token specs)
++ Unnamed inline regex (in productions)
* Unnecessary parentheses
* Use bracketed expansion instead of ()? (when there's another set of parentheses inside)
* String uncovered by literal regex
* Regex reference can be replaced by string
  * String can be replaced by regex reference
  * Only apply if the regex reference is exactly just the string and vice versa
* Collapsible regex productions (consecutive (no comment in between), same regex kind, same lexical states)
* Empty parser actions
* Consecutive parser actions
* Suspicious node descriptor expr (when it's parsable as an expansion unit, but parsed as a java expression)

### Injection

* Unnecessary token variable (used only to get the last token, exactly once)

### Control flow analysis

* Missing return statement
* Expansion sequence can/ should be factored out (also for parser actions)
* Lookahead issues (needs global CFA)

# Things not necessary before release


* Java injection
  * File-wide injection -> will depend on its resilience
  * Avoid bug with the java expressions (&&)
  * Avoid bugs with braces
  * Fix control flow in injected blocks (hard)
  * Avoid injection preventing live template insertion
  * Highlight unresolved jjtThis references (when in no node scope)
