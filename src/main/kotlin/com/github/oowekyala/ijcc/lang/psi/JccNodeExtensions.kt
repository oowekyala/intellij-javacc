package com.github.oowekyala.ijcc.lang.psi

/*
    Miscellaneous semantic extensions for jcc nodes.
 */


fun JccFile.allProductions(): Sequence<JccProduction> =
    grammarFileRoot?.childrenSequence()?.filterIsInstance<JccProduction>().orEmpty()
