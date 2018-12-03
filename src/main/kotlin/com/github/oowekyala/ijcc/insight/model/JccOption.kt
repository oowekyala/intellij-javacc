package com.github.oowekyala.ijcc.insight.model

/**
 * @author Clément Fournier
 * @since 1.0
 */
sealed class JccOption<T : Any>(override val type: JccOptionType<T>, override val staticDefaultValue: T?)
    : GenericOption<T> {


    override val name: String = javaClass.simpleName

    object PARSER_SUPER_CLASS : JccOption<String>(JccOptionType.STRING, "")
    object TOKEN_MANAGER_SUPER_CLASS : JccOption<String>(JccOptionType.STRING, "")
    object LOOKAHEAD : JccOption<Int>(JccOptionType.INTEGER, 1)
    object CHOICE_AMBIGUITY_CHECK : JccOption<Int>(JccOptionType.INTEGER, 0)
    object OTHER_AMBIGUITY_CHECK : JccOption<Int>(JccOptionType.INTEGER, 1)
    object STATIC : JccOption<Boolean>(JccOptionType.BOOLEAN, true)
    object PARSER_CODE_GENERATOR : JccOption<String>(JccOptionType.STRING, "")
    object TOKEN_MANAGER_CODE_GENERATOR : JccOption<String>(JccOptionType.STRING, "")
    object NO_DFA : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object DEBUG_PARSER : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object DEBUG_LOOKAHEAD : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object DEBUG_TOKEN_MANAGER : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object ERROR_REPORTING : JccOption<Boolean>(JccOptionType.BOOLEAN, true)
    object JAVA_UNICODE_ESCAPE : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object UNICODE_INPUT : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object IGNORE_CASE : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object USER_TOKEN_MANAGER : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object USER_CHAR_STREAM : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object BUILD_PARSER : JccOption<Boolean>(JccOptionType.BOOLEAN, true)
    object BUILD_TOKEN_MANAGER : JccOption<Boolean>(JccOptionType.BOOLEAN, true)
    object TOKEN_MANAGER_USES_PARSER : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object SANITY_CHECK : JccOption<Boolean>(JccOptionType.BOOLEAN, true)
    object FORCE_LA_CHECK : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object COMMON_TOKEN_ACTION : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object CACHE_TOKENS : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object KEEP_LINE_COLUMN : JccOption<Boolean>(JccOptionType.BOOLEAN, true)
    object GENERATE_CHAINED_EXCEPTION : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object GENERATE_GENERICS : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object GENERATE_BOILERPLATE : JccOption<Boolean>(JccOptionType.BOOLEAN, true)
    object GENERATE_STRING_BUILDER : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object GENERATE_ANNOTATIONS : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object SUPPORT_CLASS_VISIBILITY_PUBLIC : JccOption<Boolean>(JccOptionType.BOOLEAN, true)
    object OUTPUT_DIRECTORY : JccOption<String>(JccOptionType.STRING, ".")
    object JDK_VERSION : JccOption<String>(JccOptionType.STRING, "1.5")
    object TOKEN_EXTENDS : JccOption<String>(JccOptionType.STRING, "")
    object TOKEN_FACTORY : JccOption<String>(JccOptionType.STRING, "")
    object GRAMMAR_ENCODING : JccOption<String>(JccOptionType.STRING, "")
    object OUTPUT_LANGUAGE : JccOption<String>(JccOptionType.STRING, "java")
    object JAVA_TEMPLATE_TYPE : JccOption<String>(JccOptionType.STRING, "classic")
    object CPP_NAMESPACE : JccOption<String>(JccOptionType.STRING, "")
    object CPP_TOKEN_INCLUDES : JccOption<String>(JccOptionType.STRING, "")
    object CPP_PARSER_INCLUDES : JccOption<String>(JccOptionType.STRING, "")
    object CPP_TOKEN_MANAGER_INCLUDES : JccOption<String>(JccOptionType.STRING, "")
    object CPP_IGNORE_ACTIONS : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object CPP_STOP_ON_FIRST_ERROR : JccOption<Boolean>(JccOptionType.BOOLEAN, false)
    object CPP_TOKEN_MANAGER_SUPERCLASS : JccOption<String>(JccOptionType.STRING, "")
    object DEPTH_LIMIT : JccOption<Int>(JccOptionType.INTEGER, 0)
    object CPP_STACK_LIMIT : JccOption<String>(JccOptionType.STRING, "")


}