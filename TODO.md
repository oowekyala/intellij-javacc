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
++ BIG PERFORMANCE PROBLEM -> probably stop building lexical states
++ Doc is not displayed on tokens in structure view
++ The ident also gives the type in the structure view...
++ Add gutter links to node classes

* Weird behaviour of CTRL-hover of production names -> the void keyword looks like it's the ident
* Add some doc about keywords, etc
* Add gutter links from node classes to productions
* Group tokens by lexical state in structure view

* Add moar tests


* Make a good plugin description
* Make a readme
* Check with JB about Intellij copyright
* Find a license



* Java injection
  ++ File-wide injection
  ++ Avoid bug with the java expressions (&&)
  ++ Avoid bugs with braces
  ++ Fix control flow in injected blocks (hard)
  ++ Avoid injection preventing live template insertion
  ++ Adapt rich highlighting
  ++ Adapt documentation provider
  * Fix accessibility problems
  * Allow to opt-out!!

## JavaCC errors

++ Duplicate string literals
++ Duplicate named token
++ Token name "DIGITS" refers to a private (with a #) regular expression.

++ Wrong option type

## Inspection ideas

++ Unnecessary #void annotation
++ Unnecessary inline regex (in token specs)
++ Unnamed inline regex (in productions)
++ Unnecessary parentheses
* Lookahead is not at a choice point
* Use bracketed expansion instead of ()? (when there's another set of parentheses inside)
* String uncovered by literal regex
++ Regex reference can be replaced by string
  ++ String can be replaced by regex reference
  ++ Only apply if the regex reference is exactly just the string and vice versa
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
* Detect left-recursion

