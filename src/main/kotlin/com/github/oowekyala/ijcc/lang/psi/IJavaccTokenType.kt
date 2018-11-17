package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCLanguage
import com.intellij.lang.Language
import com.intellij.psi.tree.IElementType

/**
 * @author Clément Fournier
 * @since 1.0
 */
class IJavaccTokenType(id: String) : IElementType(id, Language.findInstance(JavaCCLanguage::class.java))