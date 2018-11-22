# Things missing in the javacc grammar

* {min,max} quantifiers for regex (complex_regular_expression_unit)
* "try" expansion unit

# Things to do before release

++ Fix bugs with lookahead not expanding anymore
* Rebuild the structure view
* Add quick doc for options and productions
++ Highlight scope of jjtree decoration and parser action
* Make a good plugin description
* Make a readme
* Check with JB about Intellij copyright
* Find a license

# Things not necessary before release


* Java injection
  * File-wide injection -> will depend on its resilience
  * Avoid bug with the java expressions (&&)
  * Avoid bugs with braces
  * Fix control flow in injected blocks (hard)
  * Avoid injection preventing live template insertion
  * Highlight unresolved jjtThis references (when in no node scope)
