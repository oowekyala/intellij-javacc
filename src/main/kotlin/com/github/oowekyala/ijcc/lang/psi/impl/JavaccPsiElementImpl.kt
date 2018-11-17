package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.gark87.idea.javacc.psi.JavaccPsiElement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

/**
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JavaccPsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), JavaccPsiElement