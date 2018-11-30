package com.github.oowekyala.ijcc.model

/**
 * @author Clément Fournier
 * @since 1.0
 */
sealed class JccOption<T : Any>(override val type: OptionType<T>, override val staticDefaultValue: T?)
    : GenericOption<T> {


    override val name: String = javaClass.simpleName

    object PARSER_SUPER_CLASS : JccOption<String>(OptionType.STRING, "")
    object TOKEN_MANAGER_SUPER_CLASS : JccOption<String>(OptionType.STRING, "")
    object LOOKAHEAD : JccOption<Int>(OptionType.INTEGER, 1)
    object CHOICE_AMBIGUITY_CHECK : JccOption<Int>(OptionType.INTEGER, 0)
    object OTHER_AMBIGUITY_CHECK : JccOption<Int>(OptionType.INTEGER, 1)
    object STATIC : JccOption<Boolean>(OptionType.BOOLEAN, true)
    object PARSER_CODE_GENERATOR : JccOption<String>(OptionType.STRING, "")
    object TOKEN_MANAGER_CODE_GENERATOR : JccOption<String>(OptionType.STRING, "")
    object NO_DFA : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object DEBUG_PARSER : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object DEBUG_LOOKAHEAD : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object DEBUG_TOKEN_MANAGER : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object ERROR_REPORTING : JccOption<Boolean>(OptionType.BOOLEAN, true)
    object JAVA_UNICODE_ESCAPE : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object UNICODE_INPUT : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object IGNORE_CASE : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object USER_TOKEN_MANAGER : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object USER_CHAR_STREAM : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object BUILD_PARSER : JccOption<Boolean>(OptionType.BOOLEAN, true)
    object BUILD_TOKEN_MANAGER : JccOption<Boolean>(OptionType.BOOLEAN, true)
    object TOKEN_MANAGER_USES_PARSER : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object SANITY_CHECK : JccOption<Boolean>(OptionType.BOOLEAN, true)
    object FORCE_LA_CHECK : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object COMMON_TOKEN_ACTION : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object CACHE_TOKENS : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object KEEP_LINE_COLUMN : JccOption<Boolean>(OptionType.BOOLEAN, true)
    object GENERATE_CHAINED_EXCEPTION : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object GENERATE_GENERICS : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object GENERATE_BOILERPLATE : JccOption<Boolean>(OptionType.BOOLEAN, true)
    object GENERATE_STRING_BUILDER : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object GENERATE_ANNOTATIONS : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object SUPPORT_CLASS_VISIBILITY_PUBLIC : JccOption<Boolean>(OptionType.BOOLEAN, true)
    object OUTPUT_DIRECTORY : JccOption<String>(OptionType.STRING, ".")
    object JDK_VERSION : JccOption<String>(OptionType.STRING, "1.5")
    object TOKEN_EXTENDS : JccOption<String>(OptionType.STRING, "")
    object TOKEN_FACTORY : JccOption<String>(OptionType.STRING, "")
    object GRAMMAR_ENCODING : JccOption<String>(OptionType.STRING, "")
    object OUTPUT_LANGUAGE : JccOption<String>(OptionType.STRING, "java")
    object JAVA_TEMPLATE_TYPE : JccOption<String>(OptionType.STRING, "classic")
    object CPP_NAMESPACE : JccOption<String>(OptionType.STRING, "")
    object CPP_TOKEN_INCLUDES : JccOption<String>(OptionType.STRING, "")
    object CPP_PARSER_INCLUDES : JccOption<String>(OptionType.STRING, "")
    object CPP_TOKEN_MANAGER_INCLUDES : JccOption<String>(OptionType.STRING, "")
    object CPP_IGNORE_ACTIONS : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object CPP_STOP_ON_FIRST_ERROR : JccOption<Boolean>(OptionType.BOOLEAN, false)
    object CPP_TOKEN_MANAGER_SUPERCLASS : JccOption<String>(OptionType.STRING, "")
    object DEPTH_LIMIT : JccOption<Int>(OptionType.INTEGER, 0)
    object CPP_STACK_LIMIT : JccOption<String>(OptionType.STRING, "")


}