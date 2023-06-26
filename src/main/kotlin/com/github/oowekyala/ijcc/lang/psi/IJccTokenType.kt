package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.JavaccLanguage
import com.intellij.psi.tree.IElementType

/**
 * @author Clément Fournier
 * @since 1.0
 */
class IJccTokenType(val id: String) : IElementType(id, JavaccLanguage.INSTANCE)
