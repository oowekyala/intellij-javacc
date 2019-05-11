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
    val setTokensBeforeHooks: Boolean = true
) {


    companion object {
        val FullJjtreeCompat = JavaccGenOptions(
            dontCloseBeforeLastParserAction = false,
            fixJjtThisConditionScope = false,
            setTokensBeforeHooks = false
        )
    }
}