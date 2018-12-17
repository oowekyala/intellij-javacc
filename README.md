[![Release version](https://img.shields.io/badge/release%20version-1.0--BETA-green.svg)](https://plugins.jetbrains.com/plugin/11431-javacc)

# JavaCC and JJTree plugin for IntelliJ IDEA

![Demo GIF](/demo.gif)



## Features

##### JavaCC code insight
  * Jump to declaration of productions and tokens
  * Comprehensive structure view
  * Quick documentation for productions and tokens
  * Useful code folding to hide what gets in the way

##### JJTree code insight
  * Link productions and JJTree node descriptors to their corresponding node class
  * Highlighting of the node scope of a node annotation

##### Java language injection
  * Java is injected in the embedded code fragments across the language, like parser actions
  * Code completion, quick documentation, usage resolution, and many other Java code insight features are available on the most basic level of injection
  * Compilation error checking (including type checking) and rich syntax highlighting can optionally be enabled in the plugin settings
  * Control flow analysis of the embedded Java file respects the structure of the grammar, so that control-flow related inspections work properly


To change the injection level (or disable it), go to **Settings** | **Languages & Frameworks** | **JavaCC**.


##### Inspections

Including unnecessary parentheses detection, ambiguous JJTree node descriptor, etc.

