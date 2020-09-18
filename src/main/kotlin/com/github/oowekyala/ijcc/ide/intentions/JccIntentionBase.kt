package com.github.oowekyala.ijcc.ide.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction

/**
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccIntentionBase(val name: String) : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = name

    override fun getText(): String = name

}

