package com.github.oowekyala.ijcc.insight.model

/**
 * @author Clément Fournier
 * @since 1.0
 */
@Suppress("ClassName")
sealed class JjtOption<T : Any>(override val type: JccOptionType<T>, override val staticDefaultValue: T?)
    : GenericOption<T> {


    override val name: String = javaClass.simpleName

    /** Generate sample implementations for SimpleNode and any other nodes used in the grammar.*/
    object BUILD_NODE_FILES : JjtOption<Boolean>(JccOptionType.BOOLEAN, true)

    /**Generate a multi mode parse tree. The default for this is false, generating a simple mode parse tree.*/
    object MULTI : JjtOption<Boolean>(JccOptionType.BOOLEAN, false)

    /**Instead of making each non-decorated production an indefinite node, make it void instead.*/
    object NODE_DEFAULT_VOID : JjtOption<Boolean>(JccOptionType.BOOLEAN, false)

    /**If set defines the name of a user-supplied class that will extend SimpleNode. Any tree nodes created will then be subclasses of NODE_CLASS.*/
    object NODE_CLASS : JjtOption<String>(JccOptionType.STRING, "")

    /**
     * Specify a class containing a factory method with following signature to construct nodes:
     *
     *      public static Node jjtCreate(int id)
     *
     * For backwards compatibility, the value false may also be specified, meaning that SimpleNode will be used as the factory class.
     */
    object NODE_FACTORY : JjtOption<String>(JccOptionType.STRING, "")

    /**
     * The package to generate the node classes into. The default for this is the parser package.
     */
    object NODE_PACKAGE : JjtOption<String>(JccOptionType.STRING, null) {
        override fun getDefaultValue(config: JavaccConfig): String = config.parserPackage
    }

    /**
     * The superclass for the SimpleNode class. By providing a custom superclass you may be able to avoid the need to edit the generated SimpleNode.java. See the examples/Interpreter for an example usage.
     */
    @Deprecated("jjtree deprecated")
    object NODE_EXTENDS : JjtOption<String>(JccOptionType.STRING, "")

    /**
     * The prefix used to construct node class names from node identifiers in multi mode. The default for this is AST.
     */
    object NODE_PREFIX : JjtOption<String>(JccOptionType.STRING, "AST")

    /**
     * Insert calls to user-defined parser methods on entry and exit of every node scope. See Node Scope Hooks above.
     */
    object NODE_SCOPE_HOOK : JjtOption<Boolean>(JccOptionType.BOOLEAN, false)

    /**
     * JJTree will use an alternate form of the node construction routines where it passes the parser object in. For example,
     *
     *      public static Node MyNode.jjtCreate(MyParser p, int id);
     *      MyNode(MyParser p, int id);
     */
    object NODE_USES_PARSER : JjtOption<Boolean>(JccOptionType.BOOLEAN, false)

    /**
     * Insert jjtGetFirstToken(), jjtSetFirstToken(), getLastToken(), and jjtSetLastToken() methods in SimpleNode. The FirstToken is automatically set up on entry to a node scope; the LastToken is automatically set up on exit from a node scope.
     */
    object TRACK_TOKENS : JjtOption<Boolean>(JccOptionType.BOOLEAN, false)

    /**
     * Generate code for a static parser. The default for this is true. This must be used consistently with the equivalent JavaCC options. The value of this option is emitted in the JavaCC source.
     */
    object STATIC : JjtOption<Boolean>(JccOptionType.BOOLEAN, true)

    /**
     * Insert a jjtAccept() method in the node classes, and generate a visitor implementation with an entry for every node type used in the grammar.
     */
    object VISITOR : JjtOption<Boolean>(JccOptionType.BOOLEAN, false)

    /**
     * If this option is set, it is used in the signature of the generated jjtAccept() methods and the visit() methods as the type of the data argument.
     */
    object VISITOR_DATA_TYPE : JjtOption<String>(JccOptionType.STRING, "Object")

    /**If this option is set, it is used in the signature of the generated jjtAccept() methods and the visit() methods as the return type of the method. */
    object VISITOR_RETURN_TYPE : JjtOption<String>(JccOptionType.STRING, "Object")

    /**If this option is set, it is used in the signature of the generated jjtAccept() methods and the visit() methods.*/
    object VISITOR_EXCEPTION : JjtOption<String>(JccOptionType.STRING, "")

    /**
     * By default, JJTree generates its output in the directory specified in the global OUTPUT_DIRECTORY setting. Explicitly setting this option allows the user to separate the parser from the tree files.
     */
    object JJTREE_OUTPUT_DIRECTORY : JjtOption<String>(JccOptionType.STRING, null) {
        override fun getDefaultValue(config: JavaccConfig): String = config.outputDirectory
    }

}