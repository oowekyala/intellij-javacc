package com.github.oowekyala.jjtx.preprocessor

/**
 * Compatibility options for JJTree.
 */
data class JavaccGenOptions(
    /**
     * Don't close the node scope before the last parser actions unit
     * in a scoped expansion unit. For example:
     *
     *     (Foo() { a } { b }) #Node
     *
     * JJTree inserts the closing code between `a` and `b`, which can
     * be confusing behaviour, since the stack isn't the same in `a`
     * and `b`.
     *
     * When set to true, this behaviour is changed and the node scope
     * is closed after `{b}`. This doesn't affect the scopes of productions,
     * since the last parser actions can be used to return `jjtThis`.
     */
    val dontCloseBeforeLastParserAction: Boolean = false,

    /**
     * If set to true, jjtThis is available in the closing condition of
     * its own node scope. In vanilla JJTree, #Node(jjtThis.something())
     * isn't compiled correctly.
     */
    val fixJjtThisConditionScope: Boolean = true,

    /**
     * If set to true, the tokens are set before calling the node open
     * and close hooks. This is better as the tokens are then available
     * inside those hooks.
     */
    val setTokensBeforeHooks: Boolean = true,

    /**
     * If set to true, the parser will implement the interface containing
     * the constants. This is a code smell and is kept only for compatibility
     * with Jjtree.
     */
    val implementNodeConstants: Boolean = false,

    /**
     * Use descriptive variable names for generated variables, instead
     * of Jjtree-like `jjtn000`, `jjtc000`, etc
     */
    val descriptiveVariableNames: Boolean = true,

    /**
     * Cast the exceptions at run-time to force declaration of checked exceptions.
     * If you trust your own code, set it to false exceptions to throw exceptions
     * immediately.
     */
    val castExceptions: Boolean = false
) {


    companion object {

        /**
         * Compatibility options mimicking JJTree output the closest.
         */
        val FullJjtreeCompat = JavaccGenOptions(
            dontCloseBeforeLastParserAction = false,
            fixJjtThisConditionScope = false,
            setTokensBeforeHooks = false,
            implementNodeConstants = true,
            descriptiveVariableNames = false,
            castExceptions = true
        )
    }
}

/**
 * Defaults correspond to [JavaccGenOptions.FullJjtreeCompat].
 */
data class JjtreeCompatBean(
    var fixJjtThisConditionScope: Boolean = false,
    var implementNodeConstants: Boolean = true,
    //    var dontCloseBeforeLastParserAction: Boolean = false,
    var setTokensBeforeHooks: Boolean = false,
    var descriptiveVariableNames: Boolean = false,
    var forceCheckedExceptionsDeclaration: Boolean = false
) {

    fun toModel(): JavaccGenOptions = JavaccGenOptions(
        fixJjtThisConditionScope = fixJjtThisConditionScope,
        implementNodeConstants = implementNodeConstants,
        setTokensBeforeHooks = setTokensBeforeHooks,
        descriptiveVariableNames = descriptiveVariableNames,
        castExceptions = forceCheckedExceptionsDeclaration
    )
}
