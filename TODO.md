# Things missing in the javacc grammar

* {min,max} quantifiers for regex (complex_regular_expression_unit)

# Things to do before release

++ Tone down injection in production headers
++ Fix bugs with lookahead not expanding anymore
* Fix control flow in injected blocks (hard)
* Rebuild the structure view
++ Fix that bug with the java expressions (&&)

# Things not necessary before release

* Highlight node scopes & scope of parser actions
  * Highlight unresolved jjtThis references (when in no node scope)
* File-wide java injection -> will depend on its resilience
* Check option validity
* Add quick doc for options and productions
