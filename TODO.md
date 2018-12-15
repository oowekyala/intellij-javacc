# Things missing in the javacc grammar

- [ ] {min,max} quantifiers for regex (complex_regular_expression_unit)
- [ ] "try" expansion unit

# Things to do before release

- [x] Fix bugs with lookahead not expanding anymore
- [x] Rebuild the structure view
- [x] Make new icons
- [x] Add quick doc for options and productions
- [x] Highlight scope of jjtree decoration and parser action
- [x] AUTOCOMPLETE
- [x] BIG PERFORMANCE PROBLEM -> probably stop building lexical states
- [x] Doc is not displayed on tokens in structure view
- [x] The ident also gives the type in the structure view...
- [x] Add gutter links to node classes

- [ ] Weird behaviour of CTRL-hover of production names -> the void keyword looks like it's the ident
- [ ] Add some doc about keywords, etc
- [x] Add gutter links from node classes to productions
- [ ] Group tokens by lexical state in structure view

- [ ] Add moar tests


- [x] Make a good plugin description
- [x] Make a readme
- [x] Check with JB about Intellij copyright
- [x] Find a license



- [x] Java injection
  - [x] File-wide injection
  - [x] Avoid bug with the java expressions (&&)
  - [x] Avoid bugs with braces
  - [x] Fix control flow in injected blocks (hard)
  - [x] Avoid injection preventing live template insertion
  - [x] Adapt rich highlighting
  - [x] Adapt documentation provider
  - [x] Fix accessibility problems
  - [x] Allow to opt-out!!

## JavaCC errors

- [x] Duplicate string literals
- [x] Duplicate named token
- [x] Token name "DIGITS" refers to a private (with a #) regular expression.

- [x] Wrong option type

## Inspection ideas

- [x] Unnecessary #void annotation
- [x] Unnecessary inline regex (in token specs)
- [x] Unnamed inline regex (in productions)
- [x] Unnecessary parentheses
- [ ] Lookahead is not at a choice point
- [ ] Use bracketed expansion instead of ()? (when there's another set of parentheses inside)
- [ ] String uncovered by literal regex
- [x] Regex reference can be replaced by string
  - [x] String can be replaced by regex reference
  - [x] Only apply if the regex reference is exactly just the string and vice versa
- [ ] Collapsible regex productions (consecutive (no comment in between), same regex kind, same lexical states)
- [ ] Empty parser actions
- [ ] Consecutive parser actions
- [ ] Suspicious node descriptor expr (when it's parsable as an expansion unit, but parsed as a java expression)

### Injection

- [ ] Unnecessary token variable (used only to get the last token, exactly once)

### Control flow analysis

- [ ] Missing return statement
- [ ] Expansion sequence can/ should be factored out (also for parser actions)
- [ ] Lookahead issues (needs global CFA)
- [ ] Detect left-recursion

