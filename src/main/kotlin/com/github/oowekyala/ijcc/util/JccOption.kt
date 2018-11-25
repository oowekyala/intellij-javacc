package com.github.oowekyala.ijcc.util

/**
 * @author Clément Fournier
 * @since 1.0
 */
enum class JccOption(val type: Type, val default: String) {
    PARSER_SUPER_CLASS(Type.STRING, ""),
    TOKEN_MANAGER_SUPER_CLASS(Type.STRING, ""),
    LOOKAHEAD(Type.INTEGER, "1"),

    CHOICE_AMBIGUITY_CHECK(Type.INTEGER, "0"),
    OTHER_AMBIGUITY_CHECK(Type.INTEGER, "1"),
    STATIC(Type.BOOLEAN, "true"),
    PARSER_CODE_GENERATOR(Type.STRING, ""),
    TOKEN_MANAGER_CODE_GENERATOR(Type.STRING, ""),
    NO_DFA(Type.BOOLEAN, "false"),
    DEBUG_PARSER(Type.BOOLEAN, "false"),

    DEBUG_LOOKAHEAD(Type.BOOLEAN, "false"),
    DEBUG_TOKEN_MANAGER(Type.BOOLEAN, "false"),
    ERROR_REPORTING(Type.BOOLEAN, "true"),
    JAVA_UNICODE_ESCAPE(Type.BOOLEAN, "false"),

    UNICODE_INPUT(Type.BOOLEAN, "false"),
    IGNORE_CASE(Type.BOOLEAN, "false"),
    USER_TOKEN_MANAGER(Type.BOOLEAN, "false"),
    USER_CHAR_STREAM(Type.BOOLEAN, "false"),

    BUILD_PARSER(Type.BOOLEAN, "true"),
    BUILD_TOKEN_MANAGER(Type.BOOLEAN, "true"),
    TOKEN_MANAGER_USES_PARSER(Type.BOOLEAN, "false"),
    SANITY_CHECK(Type.BOOLEAN, "true"),

    FORCE_LA_CHECK(Type.BOOLEAN, "false"),
    COMMON_TOKEN_ACTION(Type.BOOLEAN, "false"),
    CACHE_TOKENS(Type.BOOLEAN, "false"),
    KEEP_LINE_COLUMN(Type.BOOLEAN, "true"),

    GENERATE_CHAINED_EXCEPTION(Type.BOOLEAN, "false"),
    GENERATE_GENERICS(Type.BOOLEAN, "false"),
    GENERATE_BOILERPLATE(Type.BOOLEAN, "true"),
    GENERATE_STRING_BUILDER(Type.BOOLEAN, "false"),

    GENERATE_ANNOTATIONS(Type.BOOLEAN, "false"),
    SUPPORT_CLASS_VISIBILITY_PUBLIC(Type.BOOLEAN, "true"),
    OUTPUT_DIRECTORY(Type.STRING, "."),
    JDK_VERSION(Type.STRING, "1.5"),

    TOKEN_EXTENDS(Type.STRING, ""),
    TOKEN_FACTORY(Type.STRING, ""),
    GRAMMAR_ENCODING(Type.STRING, ""),
    OUTPUT_LANGUAGE(Type.STRING, "java"),

    JAVA_TEMPLATE_TYPE(Type.STRING, "classic"),
    CPP_NAMESPACE(Type.STRING, ""),
    CPP_TOKEN_INCLUDES(Type.STRING, ""),
    CPP_PARSER_INCLUDES(Type.STRING, ""),

    CPP_TOKEN_MANAGER_INCLUDES(Type.STRING, ""),
    CPP_IGNORE_ACTIONS(Type.BOOLEAN, "false"),
    CPP_STOP_ON_FIRST_ERROR(Type.BOOLEAN, "false"),
    CPP_TOKEN_MANAGER_SUPERCLASS(Type.STRING, ""),

    DEPTH_LIMIT(Type.INTEGER, "0"),
    CPP_STACK_LIMIT(Type.STRING, "");


    companion object {
        private enum class Type {
            STRING, INTEGER, BOOLEAN
        }
    }
}