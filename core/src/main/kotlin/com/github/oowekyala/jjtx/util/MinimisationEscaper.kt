package com.github.oowekyala.jjtx.util

import com.google.common.base.Strings
import com.google.common.base.Verify
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.TreeRangeMap

// References to some classes that the JAR minimisation removes otherwise...
// This is obviously shitty, Idk why but the exclude specs don't work on guava

private object MinimisationEscaper {

    val trm: TreeRangeMap<*, *>? = null
    val verif: Verify? = null
    val almm: ArrayListMultimap<*,*>? = null
    val almmde: Strings? = null

}

