package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.intellij.psi.JavaTokenType
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.tree.JavaDocElementType
import com.intellij.psi.tree.TokenSet

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccTypesExt {
    val IdentifierTypeSet = TokenSet.create(JavaccTypes.JCC_IDENT)

    val CommentTypeSet = TokenSet.create(
        JavaTokenType.END_OF_LINE_COMMENT,
        JavaTokenType.C_STYLE_COMMENT,
        JavaDocElementType.DOC_COMMENT
    )

    val StringLiteralTypeSet = TokenSet.create(JavaccTypes.JCC_STRING_LITERAL)

    val WhitespaceTypeSet = TokenSet.create(TokenType.WHITE_SPACE)
}
