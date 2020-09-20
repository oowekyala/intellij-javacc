package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.model.JccOptionType.BaseOptionType.*

/**
 * @author Clément Fournier
 * @since 1.0
 */
@Suppress("ClassName", "unused")
sealed class J21Option<T : Any>(type: JccOptionType<T>, staticDefaultValue: T?)
    : GenericOption<T>(type, staticDefaultValue, GrammarNature.JJTREE) {

    override val name: String = javaClass.simpleName

    object SPECIAL_TOKENS_ARE_NODES : J21Option<Boolean>(BOOLEAN, false)

    object DEFAULT_LEXICAL_STATE : J21Option<String>(STRING, "DEFAULT")
    object TABS_TO_SPACE : J21Option<Int>(INTEGER, 4)

    /*
    SPECIAL_TOKENS_ARE_NODES;
    PARSER_PACKAGE=javagrammar;
    DEFAULT_LEXICAL_STATE=JAVA;
    PRESERVE_LINE_ENDINGS=false;
    TABS_TO_SPACES=8;
     */

    companion object {
        val values = listOf(
            SPECIAL_TOKENS_ARE_NODES,
            DEFAULT_LEXICAL_STATE,
            TABS_TO_SPACE
        )
    }

}
