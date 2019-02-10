package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.model.JccOptionType.BaseOptionType.*
import com.github.oowekyala.ijcc.lang.model.JccOptionType.RefinedOptionType

/**
 * @author Clément Fournier
 * @since 1.0
 */
@Suppress("unused", "ClassName")
sealed class JccOption<T : Any>(type: JccOptionType<T>, staticDefaultValue: T) :
    GenericOption<T>(type, staticDefaultValue) {


    override val name: String = javaClass.simpleName


    /**
     * The number of tokens to look ahead before making a decision a
     *t
     * a choice point during parsing. The default value is 1. The smaller
     * this number, the faster the parser. This number may be overridden
     * for specific productions within the grammar as described later.
     * See the description of the lookahead algorithm for complete details
     * on how lookahead works.
     */
    object LOOKAHEAD : JccOption<Int>(INTEGER, 1)

    /**
     * This is an integer option whose default value is 2. This is the
     * number of tokens considered in checking choices of the form "A
     * | B | ..." for ambiguity. For example, if there is a common two
     * token prefix for both A and B, but no common three token prefix,
     * (assume this option is set to 3) then JavaCC can tell you to
     * use a lookahead of 3 for disambiguation purposes. And if A and
     * B have a common three token prefix, then JavaCC only tell you
     * that you need to have a lookahead of 3 or more. Increasing this
     * can give you more comprehensive ambiguity information at the
     * cost of more processing time. For large grammars such as the
     * Java grammar, increasing this number any further causes the checking
     * to take too much time.
     */
    object CHOICE_AMBIGUITY_CHECK : JccOption<Int>(INTEGER, 0)

    /**
     * This is an integer option whose default value is 1. This is the
     * number of tokens considered in checking all other kinds of choices
     * (i.e., of the forms "(A)*", "(A)+", and "(A)?") for ambiguity.
     * This takes more time to do than the choice checking, and hence
     * the default value is set to 1 rather than 2.
     */
    object OTHER_AMBIGUITY_CHECK : JccOption<Int>(INTEGER, 1)

    /**
     * This is a boolean option whose default value is true. If true,
     * all methods and class variables are specified as static in the
     * generated parser and token manager. This allows only one parser
     * object to be present, but it improves the performance of the
     * parser. To perform multiple parses during one run of your Java
     * program, you will have to call the ReInit() method to reinitialize
     * your parser if it is static. If the parser is non-static, you
     * may use the "new" operator to construct as many parsers as you
     * wish. These can all be used simultaneously from different threads
     *.
     */
    object STATIC : JccOption<Boolean>(BOOLEAN, true)


    /**
     * This is a boolean option whose default value is false. This option
     * is used to obtain debugging information from the generated parser.
     * Setting this option to true causes the parser to generate a trace
     * of its actions. Tracing may be disabled by calling the method
     * disable_tracing() in the generated parser class. Tracing may
     * be subsequently enabled by calling the method enable_tracing()
     * in the generated parser class.
     */
    object DEBUG_PARSER : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is false. Setting
     * this option to true causes the parser to generate all the tracing
     * information it does when the option DEBUG_PARSER is true, and
     * in addition, also causes it to generated a trace of actions performed
     * during lookahead operation.
     */
    object DEBUG_LOOKAHEAD : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is false. This option
     * is used to obtain debugging information from the generated token
     * manager. Setting this option to true causes the token manager
     * to generate a trace of its actions. This trace is rather large
     * and should only be used when you have a lexical error that has
     * been reported to you and you cannot understand why. Typically,
     * in this situation, you can determine the problem by looking at
     * the last few lines of this trace.
     */
    object DEBUG_TOKEN_MANAGER : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is true. Setting
     * it to false causes errors due to parse errors to be reported
     * in somewhat less detail. The only reason to set this option to
     * false is to improve performance.
     */
    object ERROR_REPORTING : JccOption<Boolean>(BOOLEAN, true)

    /**
     * This is a boolean option whose default value is false. When set
     * to true, the generated parser uses an input stream object that
     * processes Java Unicode escapes (\u...) before sending characters
     * to the token manager. By default, Java Unicode escapes are not
     * processed.
     * This option is ignored if either of options USER_TOKEN_MANAGER,
     * USER_CHAR_STREAM is set to true.
     */
    object JAVA_UNICODE_ESCAPE : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is false. When set
     * to true, the generated parser uses uses an input stream object
     * that reads Unicode files. By default, ASCII files are assumed
     *.
     * This option is ignored if either of options USER_TOKEN_MANAGER,
     * USER_CHAR_STREAM is set to true.
     */
    object UNICODE_INPUT : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is false. Setting
     * this option to true causes the generated token manager to ignore
     * case in the token specifications and the input files. This is
     * useful for writing grammars for languages such as HTML. It is
     * also possible to localize the effect of IGNORE_CASE by using
     * an alternate mechanism described later.
     */
    object IGNORE_CASE : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is false. The defaul
     *t
     * action is to generate a token manager that works on the specified
     * grammar tokens. If this option is set to true, then the parser
     * is generated to accept tokens from any token manager of type
     * "TokenManager" - this interface is generated into the generated
     * parser directory.
     */
    object USER_TOKEN_MANAGER : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is false. The defaul
     *t
     * action is to generate a character stream reader as specified
     * by the options JAVA_UNICODE_ESCAPE and UNICODE_INPUT. The generated
     * token manager receives characters from this stream reader. If
     * this option is set to true, then the token manager is generated
     * to read characters from any character stream reader of type "CharStream.java".
     * This file is generated into the generated parser directory.
     *
     * This option is ignored if USER_TOKEN_MANAGER is set to true.
     *
     */
    object USER_CHAR_STREAM : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is true. The default
     * action is to generate the parser file ("MyParser.java" in the
     * above example). When set to false, the parser file is not generated.
     * Typically, this option is set to false when you wish to generate
     * only the token manager and use it without the associated parser
     *.
     */
    object BUILD_PARSER : JccOption<Boolean>(BOOLEAN, true)

    /**
     *  This is a boolean option whose default value is true. The default
     * action is to generate the token manager file ("MyParserTokenManager.java"
     * in the above example). When set to false the token manager file
     * is not generated. The only reason to set this option to false
     * is to save some time during parser generation when you fix problems
     * in the parser part of the grammar file and leave the lexical
     * specifications untouched.
     */
    object BUILD_TOKEN_MANAGER : JccOption<Boolean>(BOOLEAN, true)

    /**
     * This is a boolean option whose default value is false. When set
     * to true, the generated token manager will include a field called
     * parser that references the instantiating parser instance (of
     * type MyParser in the above example). The main reason for having
     * a parser in a token manager is using some of its logic in lexical
     * actions. This option has no effect if the STATIC option is set
     * to true.
     */
    object TOKEN_MANAGER_USES_PARSER : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is true. JavaCC
     * performs many syntactic and semantic checks on the grammar file
     * during parser generation. Some checks such as detection of left
     * recursion, detection of ambiguity, and bad usage of empty expansions
     * may be suppressed for faster parser generation by setting this
     * option to false. Note that the presence of these errors (even
     * if they are not detected and reported by setting this option
     * to false) can cause unexpected behavior from the generated parser
     *.
     *
     */
    object SANITY_CHECK : JccOption<Boolean>(BOOLEAN, true)

    /**
     * This is a boolean option whose default value is false. This option
     * setting controls lookahead ambiguity checking performed by JavaCC.
     * By default (when this option is false), lookahead ambiguity checking
     * is performed for all choice points where the default lookahead
     * of 1 is used. Lookahead ambiguity checking is not performed at
     * choice points where there is an explicit lookahead specification,
     * or if the option LOOKAHEAD is set to something other than 1.
     * Setting this option to true performs lookahead ambiguity checking
     * at all choice points regardless of the lookahead specifications
     * in the grammar file.
     */
    object FORCE_LA_CHECK : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is false. When set
     * to true, every call to the token manager's method "getNextToken"
     * (see the description of the Java Compiler Compiler API) will
     * cause a call to a used defined method "CommonTokenAction" after
     * the token has been scanned in by the token manager. The user
     * must define this method within the TOKEN_MGR_DECLS section. The
     * signature of this method is:
     *
     *    void CommonTokenAction(Token t)
     */
    object COMMON_TOKEN_ACTION : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is false. Setting
     * this option to true causes the generated parser to lookahead
     * for extra tokens ahead of time. This facilitates some performance
     * improvements. However, in this case (when the option is true),
     * interactive applications may not work since the parser needs
     * to work synchronously with the availability of tokens from the
     * input stream. In such cases, it's best to leave this option at
     * its default value.
     */
    object CACHE_TOKENS : JccOption<Boolean>(BOOLEAN, false)

    /**
     * This is a boolean option whose default value is true. The default
     * action is to generate support classes (such as Token.java, ParseException.java
     * etc) with Public visibility. If set to false, the classes will
     * be generated with package-private visibility.
     */
    object SUPPORT_CLASS_VISIBILITY_PUBLIC : JccOption<Boolean>(BOOLEAN, true)

    /**
     * This is a string valued option whose default value is the current
     * directory. This controls where output files are generated.
     */
    object OUTPUT_DIRECTORY : JccOption<String>(RefinedOptionType.DIRECTORY, ".")

    /**
     * This is a string option whose default value is "", meaning that
     * the generated Token class will extend java.lang.Object. This
     * option may be set to the name of a class that will be used as
     * the base class for the generated Token class.
     */
    object TOKEN_EXTENDS : JccOption<String>(STRING, "")

    /**
     * This is a string option whose default value is "", meaning that
     * Tokens will be created by calling Token.newToken(). If set the
     * option names a Token factory class containing a public static
     * Token newToken(int ofKind, String image) method.
     */
    object TOKEN_FACTORY : JccOption<String>(STRING, "")


    /**
     * Style of java code generation.
     *
     * The "classic" style is tightly coupled to Java IO classes - not GWT compatible.
     */
    object JAVA_TEMPLATE_TYPE : JccOption<String>(STRING, "classic")

    /**
     * Return the file encoding (e.g., UTF-8, ISO_8859-1, MacRoman); this will return the file.encoding system
     * property if no value was explicitly set
     */
    object GRAMMAR_ENCODING
        : JccOption<String>(STRING, "") {

        override fun defaultValueFallback(config: GrammarOptions): String =
            System.getProperties().getProperty("file.encoding")
    }

    object JDK_VERSION : JccOption<String>(STRING, "1.5")


    object PARSER_SUPER_CLASS : JccOption<String>(RefinedOptionType.TYPE, "") {
        override fun defaultValueFallback(config: GrammarOptions): String = JLangObject
    }

    object TOKEN_MANAGER_SUPER_CLASS : JccOption<String>(RefinedOptionType.TYPE, "") {
        override fun defaultValueFallback(config: GrammarOptions): String = JLangObject
    }

    object PARSER_CODE_GENERATOR : JccOption<String>(RefinedOptionType.TYPE, "") {
        override fun defaultValueFallback(config: GrammarOptions): String = JLangObject
    }

    object TOKEN_MANAGER_CODE_GENERATOR : JccOption<String>(RefinedOptionType.TYPE, "") {
        override fun defaultValueFallback(config: GrammarOptions): String = JLangObject
    }

    // undocumented options...

    object KEEP_LINE_COLUMN : JccOption<Boolean>(BOOLEAN, true)

    object GENERATE_CHAINED_EXCEPTION : JccOption<Boolean>(BOOLEAN, false)
    object GENERATE_GENERICS : JccOption<Boolean>(BOOLEAN, false)
    object GENERATE_BOILERPLATE : JccOption<Boolean>(BOOLEAN, true)
    object GENERATE_STRING_BUILDER : JccOption<Boolean>(BOOLEAN, false)
    object GENERATE_ANNOTATIONS : JccOption<Boolean>(BOOLEAN, false)


    object NO_DFA : JccOption<Boolean>(BOOLEAN, false)

    object DEPTH_LIMIT : JccOption<Int>(INTEGER, 0)

    // These are only for CPP, which the plugin doesn't support

    object OUTPUT_LANGUAGE : JccOption<String>(STRING, "java")

    object CPP_NAMESPACE : JccOption<String>(STRING, "")
    object CPP_TOKEN_INCLUDES : JccOption<String>(STRING, "")
    object CPP_PARSER_INCLUDES : JccOption<String>(STRING, "")
    object CPP_TOKEN_MANAGER_INCLUDES : JccOption<String>(STRING, "")
    object CPP_IGNORE_ACTIONS : JccOption<Boolean>(BOOLEAN, false)
    object CPP_STOP_ON_FIRST_ERROR : JccOption<Boolean>(BOOLEAN, false)
    object CPP_TOKEN_MANAGER_SUPERCLASS : JccOption<String>(STRING, "")
    object CPP_STACK_LIMIT : JccOption<String>(STRING, "")


    companion object {
        private const val JLangObject = "java.lang.Object"

        val values = listOf(
            PARSER_SUPER_CLASS,
            TOKEN_MANAGER_SUPER_CLASS,
            LOOKAHEAD,

            CHOICE_AMBIGUITY_CHECK,
            OTHER_AMBIGUITY_CHECK,
            STATIC,
            PARSER_CODE_GENERATOR,
            TOKEN_MANAGER_CODE_GENERATOR,
            NO_DFA,
            DEBUG_PARSER,

            DEBUG_LOOKAHEAD,
            DEBUG_TOKEN_MANAGER,
            ERROR_REPORTING,
            JAVA_UNICODE_ESCAPE,

            UNICODE_INPUT,
            IGNORE_CASE,
            USER_TOKEN_MANAGER,
            USER_CHAR_STREAM,

            BUILD_PARSER,
            BUILD_TOKEN_MANAGER,
            TOKEN_MANAGER_USES_PARSER,
            SANITY_CHECK,

            FORCE_LA_CHECK,
            COMMON_TOKEN_ACTION,
            CACHE_TOKENS,
            KEEP_LINE_COLUMN,

            GENERATE_CHAINED_EXCEPTION,
            GENERATE_GENERICS,
            GENERATE_BOILERPLATE,
            GENERATE_STRING_BUILDER,

            GENERATE_ANNOTATIONS,
            SUPPORT_CLASS_VISIBILITY_PUBLIC,
            OUTPUT_DIRECTORY,
            JDK_VERSION,

            TOKEN_EXTENDS,
            TOKEN_FACTORY,
            GRAMMAR_ENCODING,
            OUTPUT_LANGUAGE,

            JAVA_TEMPLATE_TYPE,
            CPP_NAMESPACE,
            CPP_TOKEN_INCLUDES,
            CPP_PARSER_INCLUDES,

            CPP_TOKEN_MANAGER_INCLUDES,
            CPP_IGNORE_ACTIONS,
            CPP_STOP_ON_FIRST_ERROR,
            CPP_TOKEN_MANAGER_SUPERCLASS,

            DEPTH_LIMIT,
            CPP_STACK_LIMIT
        )

    }
}