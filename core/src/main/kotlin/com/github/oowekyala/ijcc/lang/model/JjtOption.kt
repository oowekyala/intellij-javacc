package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.model.JccOptionType.BaseOptionType.*
import com.github.oowekyala.ijcc.lang.model.JccOptionType.RefinedOptionType
import com.github.oowekyala.ijcc.lang.model.JccOptionType.RefinedOptionType.PACKAGE
import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding
import com.github.oowekyala.ijcc.lang.psi.matchesType
import com.github.oowekyala.ijcc.lang.psi.stringValue

/**
 * @author Clément Fournier
 * @since 1.0
 */
@Suppress("ClassName", "unused")
sealed class JjtOption<T : Any>(type: JccOptionType<T>, staticDefaultValue: T?)
    : GenericOption<T>(type, staticDefaultValue, GrammarNature.JJTREE) {

    // TODO jjtree mod -> allow configuring SimpleNode name

    override val name: String = javaClass.simpleName

    /**  Generate sample implementations for SimpleNode and any other nodes used in the grammar. */
    object BUILD_NODE_FILES : JjtOption<Boolean>(BOOLEAN, true)

    /** Generate a multi mode parse tree. The default for this is false, generating a simple mode parse tree. */
    object MULTI : JjtOption<Boolean>(BOOLEAN, false)

    /** Instead of making each non-decorated production an indefinite node, make it void instead. */
    object NODE_DEFAULT_VOID : JjtOption<Boolean>(BOOLEAN, false)

    /**
     * If set defines the name of a user-supplied class that will extend SimpleNode. Any tree nodes created will then be subclasses of NODE_CLASS.
     * If set, also changes the type of the jjtThis references to only be that.
     */
    object NODE_CLASS : JjtOption<String>(RefinedOptionType.TYPE, "") {
        override fun contextualDefaultValue(config: IGrammarOptions): String = config.addPackage("SimpleNode")
    }

    /**
     *  Specify a class containing a factory method with following signature
     *  to construct nodes:
     *
     *      public static Node jjtCreate(int id)
     *
     *  For backwards compatibility, the value false may also be specified,
     *  meaning that SimpleNode will be used as the factory class.
     */
    object NODE_FACTORY : JjtOption<String>(RefinedOptionType.TYPE, "") {
        override fun getValue(optionBinding: JccOptionBinding?, config: IGrammarOptions): String =
            // the actual default
            if (optionBinding == null
                || optionBinding.matchesType(INTEGER)
                || optionBinding.matchesType(BOOLEAN)
                || optionBinding.matchesType(STRING) && optionBinding.stringValue.isEmpty()
            )
                config.addPackage("SimpleNode")
            else optionBinding.stringValue
    }

    /**
     *  The package to generate the node classes into. The default for
     *  this is the parser package.
     */
    object NODE_PACKAGE : JjtOption<String>(PACKAGE, null) {
        override fun contextualDefaultValue(config: IGrammarOptions): String = config.parserPackage
    }

    /**
     *  The superclass for the SimpleNode class. By providing a custom
     *  superclass you may be able to avoid the need to edit the generated
     *  SimpleNode.java. See the examples/Interpreter for an example
     *  usage.
     */
    // @Deprecated("Deprecated in JJTree")
    object NODE_EXTENDS : JjtOption<String>(RefinedOptionType.TYPE, "")

    /**
     *  The prefix used to construct node class names from node identifiers
     *  in multi mode. The default for this is AST.
     */
    object NODE_PREFIX : JjtOption<String>(STRING, "AST")

    /**
     *  Insert calls to user-defined parser methods on entry and exit
     *  of every node scope. See Node Scope Hooks above.
     */
    object NODE_SCOPE_HOOK : JjtOption<Boolean>(BOOLEAN, false)

    /**
     *  JJTree will use an alternate form of the node construction routines
     *  where it passes the parser object in. For example,
     *
     *      public static Node MyNode.jjtCreate(MyParser p, int id);
     *
     *      MyNode(MyParser p, int id);
     */
    object NODE_USES_PARSER : JjtOption<Boolean>(BOOLEAN, false)

    /**
     *  Insert jjtGetFirstToken(), jjtSetFirstToken(), getLastToken(),
     *  and jjtSetLastToken() methods in SimpleNode. The FirstToken
     *  is automatically set up on entry to a node scope; the LastToken
     *  is automatically set up on exit from a node scope.
     */
    object TRACK_TOKENS : JjtOption<Boolean>(BOOLEAN, false)

    /**
     *  Generate code for a static parser. The default for this is true.
     *  This must be used consistently with the equivalent JavaCC options.
     *  The value of this option is emitted in the JavaCC source.
     */
    object STATIC : JjtOption<Boolean>(BOOLEAN, true)

    /**
     *  Insert a jjtAccept() method in the node classes, and generate
     *  a visitor implementation with an entry for every node type used
     *  in the grammar.
     */
    object VISITOR : JjtOption<Boolean>(BOOLEAN, false)


    object VISITOR_METHOD_NAME_INCLUDES_TYPE_NAME : JjtOption<Boolean>(BOOLEAN, false)

    /**
     *  If this option is set, it is used in the signature of the generated
     *  jjtAccept() methods and the visit() methods as the type of the
     *  data argument.
     */
    object VISITOR_DATA_TYPE : JjtOption<String>(RefinedOptionType.TYPE, "Object")

    /** If this option is set, it is used in the signature of the generated jjtAccept() methods and the visit() methods as the return type of the method. */
    object VISITOR_RETURN_TYPE : JjtOption<String>(RefinedOptionType.TYPE, "Object")

    /** If this option is set, it is used in the signature of the generated jjtAccept() methods and the visit() methods. */
    object VISITOR_EXCEPTION : JjtOption<String>(RefinedOptionType.TYPE, "")

    /**
     *  By default, JJTree generates its output in the directory specified
     *  in the global OUTPUT_DIRECTORY setting. Explicitly setting this
     *  option allows the user to separate the parser from the tree
     *  files.
     */
    object JJTREE_OUTPUT_DIRECTORY : JjtOption<String>(RefinedOptionType.DIRECTORY, ".") {
        override fun contextualDefaultValue(config: IGrammarOptions): String = config.inlineBindings.outputDirectory
    }


    companion object {
        val values = listOf(
            MULTI,
            NODE_DEFAULT_VOID,
            NODE_SCOPE_HOOK,
            NODE_USES_PARSER,
            BUILD_NODE_FILES,
            VISITOR,
            VISITOR_METHOD_NAME_INCLUDES_TYPE_NAME,
            TRACK_TOKENS,

            NODE_PREFIX,
            NODE_PACKAGE,
            NODE_EXTENDS,
            NODE_CLASS,
            NODE_FACTORY,
            // NODE_INCLUDES, TODO???
            // OUTPUT_FILE, TODO???
            VISITOR_DATA_TYPE,
            VISITOR_RETURN_TYPE,
            VISITOR_EXCEPTION,

            JJTREE_OUTPUT_DIRECTORY
        )
    }

}
