package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.JccTypes.*
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccTypesExt : JccTypes {

    val IdentifierTypeSet = TokenSet.create(JCC_IDENT)

    val CommentTypeSet = TokenSet.create(
        JCC_END_OF_LINE_COMMENT,
        JCC_C_STYLE_COMMENT
    )

    val StringLiteralTypeSet = TokenSet.create(JCC_STRING_LITERAL)

    val WhitespaceTypeSet = TokenSet.create(TokenType.WHITE_SPACE)

}
