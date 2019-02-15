[![](https://img.shields.io/jetbrains/plugin/v/11431-a8translate.svg)](https://plugins.jetbrains.com/plugin/11431-javacc)

# JavaCC and JJTree plugin for IntelliJ IDEA


Offers comprehensive language support for the [JavaCC](https://github.com/javacc/javacc) parser generator. Doesn't generate parsers itself yet though. Code ide works best when the token manager and other supporting files have already been generated.


![Demo GIF](/demo.gif)



## Features

##### JavaCC code insight
  * Jump to declaration of productions and tokens
  * Comprehensive structure view
  * Quick documentation for productions and tokens
  * Useful code folding to hide what gets in the way
  * Detection of all JavaCC errors as you type

##### JJTree code insight
  * Link JJTree node descriptors to their corresponding node class, and vice-versa
  * Highlighting of the node scope of a node annotation
  * Jump to partial declarations when they exist

##### Java language injection
  * Java is injected in the embedded code fragments across the language, like parser actions
  * Code completion, quick documentation, usage resolution, and many other Java IDE features are available on the most basic level of injection
  * Compilation error checking (including type checking) and rich syntax highlighting can optionally be enabled in the plugin settings
  * Control flow analysis of the embedded Java file respects the structure of the grammar, so that control-flow related inspections work properly


To change the injection level (or disable injection), go to **Settings** | **Languages & Frameworks** | **JavaCC**.


##### Inspections and intention actions

Including unnecessary parentheses detection, unreachable production detection, ambiguous JJTree node descriptor, etc.
Many JavaCC warnings are also implemented as inspections. Inspections can be suppressed since version 1.0.

## Usage notes

* I recommend to define an easy shortcut for fold/unfold region from the start if you don't have any
* Code insight in Java fragments works best when the token manager and other supporting files have already been generated.
* Please <a href="https://github.com/oowekyala/intellij-javacc/issues">report issues</a> if you encounter any.
  The Java injection support in particular is still quite fragile. Contributions are most welcome as well!
