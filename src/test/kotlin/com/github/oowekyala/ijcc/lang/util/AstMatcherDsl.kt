package com.github.oowekyala.ijcc.lang.util

import io.kotlintest.Matcher
import io.kotlintest.shouldNotBe
import kotlin.test.assertFalse
import kotlin.test.assertTrue


typealias MatcherPath<H> = List<NWrapper<H, out H>>

/**
 * Wraps a node, providing easy access to [it]. Additional matching
 * methods are provided to match children. This DSL supports objects
 * of any type provided they can be viewed as nodes of a tree. The
 * [TreeLikeAdapter] type class witnesses this property.
 *
 *
 * @property it         Wrapped node
 * @param adapter       Instance of the [TreeLikeAdapter] type class for the [H] type hierarchy
 * @param parentPath    List of the parents of this node, (not including the self), used to reconstruct a path for error messages
 * @param childMatchersAreIgnored Ignore calls to [child]
 *
 * @param H Hierarchy of the node
 * @param N Type of the node
 */
class NWrapper<H : Any, N : H> private constructor(val it: N,
                                                   private val adapter: TreeLikeAdapter<H>,
                                                   private val parentPath: MatcherPath<H>,
                                                   private val childMatchersAreIgnored: Boolean) {


    private val myChildren = adapter.getChildren(it)
    private val pathToMe = parentPath + this
    private val myNumChildren = myChildren.size

    /** Index to which the next child matcher will apply. */
    private var nextChildMatcherIdx = 0

    private fun shiftChild(num: Int = 1): H {

        checkChildExists(nextChildMatcherIdx)

        val ret = myChildren[nextChildMatcherIdx]

        nextChildMatcherIdx += num
        return ret
    }


    private fun checkChildExists(childIdx: Int) =
            assertTrue(
                formatErrorMessage(
                    adapter,
                    parentPath,
                    "Node has fewer children than expected, child #$childIdx doesn't exist"
                )
            ) {
                childIdx in 0..myChildren.size
            }


    /**
     * Specify that the next [num] children will only be tested for existence,
     * but not for type, or anything else.
     */
    fun unspecifiedChildren(num: Int) {
        shiftChild(num)
        // Checks that the last child mentioned exists
        checkChildExists(nextChildMatcherIdx - 1)
    }


    /**
     * Specify that the next child will only be tested for existence,
     * but not for type, or anything else.
     */
    fun unspecifiedChild() = unspecifiedChildren(1)


    /**
     * Specify that the next child will be tested against the assertions
     * defined by the lambda.
     *
     * This method asserts that the child exists, and that it is of the
     * required type [M]. The lambda is then executed on it. Subsequent
     * calls to this method at the same tree level will test the next
     * children.
     *
     * @param ignoreChildren If true, the number of children of the child is not asserted.
     *                       Calls to [child] in the [nodeSpec] throw an exception.
     * @param nodeSpec Sequence of assertions to carry out on the child node
     *
     * @param M Expected type of the child
     *
     * @throws AssertionError If the child is not of type [M], or fails the assertions of the [nodeSpec]
     * @return The child, if it passes all assertions, otherwise throws an exception
     */
    inline fun <reified M : H> child(ignoreChildren: Boolean = false, noinline nodeSpec: NWrapper<H, M>.() -> Unit): M =
            childImpl(ignoreChildren, M::class.java) { nodeSpec(); it }

    /**
     * Specify that the next child will be tested against the assertions
     * defined by the lambda, and returns the return value of the lambda.
     *
     * This method asserts that the child exists, and that it is of the
     * required type [M]. The lambda is then executed on it. Subsequent
     * calls to this method at the same tree level will test the next
     * children.
     *
     * @param ignoreChildren If true, the number of children of the child is not asserted.
     *                       Calls to [child] in the [nodeSpec] throw an exception.
     * @param nodeSpec Sequence of assertions to carry out on the child node
     *
     * @param M Expected type of the child
     * @param R Return type of the call
     *
     * @throws AssertionError If the child is not of type [M], or fails the assertions of the [nodeSpec]
     * @return The return value of the lambda
     */
    inline fun <reified M : H, R> childRet(ignoreChildren: Boolean = false,
                                           noinline nodeSpec: NWrapper<H, M>.() -> R): R =
            childImpl(ignoreChildren, M::class.java, nodeSpec)


    @PublishedApi
    internal fun <M : H, R> childImpl(ignoreChildren: Boolean,
                                      childType: Class<M>,
                                      nodeSpec: NWrapper<H, M>.() -> R): R {
        if (!childMatchersAreIgnored)
            return executeWrapper(adapter, childType, shiftChild(), pathToMe, ignoreChildren, nodeSpec)
        else
            throw IllegalStateException(
                formatErrorMessage(
                    adapter,
                    pathToMe,
                    "Calling child when ignoreChildren=true is forbidden"
                )
            )
    }


    override fun toString(): String {
        return "NWrapper<${adapter.nodeName(it::class.java)}>"
    }


    companion object {


        private fun <H : Any> formatPath(adapter: TreeLikeAdapter<H>, matcherPath: MatcherPath<H>) =
                when {
                    matcherPath.isEmpty() -> "<root>"
                    else                  -> matcherPath.joinToString(separator = "/", prefix = "/") {
                        adapter.nodeName(it.it)
                    }
                }

        private fun <H : Any> formatErrorMessage(adapter: TreeLikeAdapter<H>,
                                                 matcherPath: MatcherPath<H>,
                                                 message: String) =
                "At ${formatPath(adapter, matcherPath)}: $message"

        /**
         * Execute wrapper assertions on a node.
         *
         * @param childType Expected type of [toWrap]
         * @param toWrap Node on which to execute the assertions
         * @param parentPath List of types of the parents of this node, used to reconstruct a path for error messages
         * @param ignoreChildrenMatchers Ignore the children matchers in [spec]
         * @param spec Assertions to carry out on [toWrap]
         *
         * @param H Hierarchy of the node
         * @param M Expected type of [toWrap]
         * @param R Return type
         *
         * @throws AssertionError If some assertions fail
         * @return [toWrap], if it passes all assertions, otherwise throws an exception
         */
        @PublishedApi
        internal fun <H : Any, M : H, R> executeWrapper(adapter: TreeLikeAdapter<H>,
                                                        childType: Class<M>,
                                                        toWrap: H,
                                                        parentPath: MatcherPath<H>,
                                                        ignoreChildrenMatchers: Boolean,
                                                        spec: NWrapper<H, M>.() -> R): R {

            val nodeNameForMsg = when {
                parentPath.isEmpty() -> "root"
                else                 -> "child #${parentPath.last().myChildren.indexOf(toWrap)}"
            }

            assertTrue(
                formatErrorMessage(
                    adapter,
                    parentPath,
                    "Expected $nodeNameForMsg to have type ${adapter.nodeName(childType)}, actual ${adapter.nodeName(
                        toWrap.javaClass
                    )}"
                )
            ) {
                childType.isInstance(toWrap)
            }

            @Suppress("UNCHECKED_CAST")
            val m = toWrap as M

            val wrapper = NWrapper(m, adapter, parentPath, ignoreChildrenMatchers)

            val ret: R = try {
                wrapper.spec()
            } catch (e: AssertionError) {
                if (e.message?.matches("At (/.*?|<root>):.*".toRegex()) == false) {
                    // the exception has no path, let's add one
                    throw AssertionError(
                        formatErrorMessage(
                            adapter, wrapper.pathToMe, e.message
                                ?: "No explanation provided"
                        ), e
                    )
                }
                throw e
            }

            assertFalse(
                formatErrorMessage(
                    adapter,
                    wrapper.pathToMe,
                    "Wrong number of children, expected ${wrapper.nextChildMatcherIdx}, actual ${wrapper.myNumChildren}"
                )
            ) {
                !ignoreChildrenMatchers && wrapper.nextChildMatcherIdx != wrapper.myNumChildren
            }
            return ret
        }
    }
}

/** Type class for something that can behave as a node in a tree. */
interface TreeLikeAdapter<H : Any> {

    /** Returns all children of the given node. */
    fun getChildren(node: H): List<H>

    /** Gets the display name of this type of node. */
    fun nodeName(type: H): String = nodeName(type::class.java)

    /** Gets the display name of this type of node. */
    fun nodeName(type: Class<out H>): String

}

/**
 * Base method to produce a kotlintest [Matcher] for nodes of a hierarchy [H]. The [H] type
 * parameter and the [adapter] are unnecessary when the domain is known, so concrete DSLs
 * should provide their own method that delegates to this one and provides the adapter.
 */


/**
 * Matcher for a node, using [NWrapper] to specify a subtree against which
 * the tested node will be tested.
 *
 * Use it with [io.kotlintest.should], e.g. `node should matchNode<ASTExpression> {}`.
 *
 *
 * @param H Considered hierarchy
 * @param N Expected type of the node
 *
 * @param adapter Instance of the [TreeLikeAdapter] type class for the [H] hierarchy
 *
 * @param ignoreChildren If true, calls to [NWrapper.child] in the [nodeSpec] are forbidden.
 *                       The number of children of the child is not asserted.
 *
 * @param nodeSpec Sequence of assertions to carry out on the node, which can be referred to by [NWrapper.it].
 *                 Assertions may consist of [NWrapper.child] calls, which perform the same type of node
 *                 matching on a child of the tested node.
 *
 * @return A matcher for AST nodes, suitable for use by [io.kotlintest.should].
 *
 * ### Samples
 *
 *    node should matchNode<ASTStatement> {
 *
 *        // nesting matchers allow to specify a whole subtree
 *        child<ASTForStatement> {
 *
 *            // This would fail if the first child of the ForStatement wasn't a ForInit
 *            child<ASTForInit> {
 *                child<ASTLocalVariableDeclaration> {
 *
 *                    // If the parameter ignoreChildren is set to true, the number of children is not asserted
 *                    // Calls to "child" in the block are forbidden
 *                    // The only checks carried out here are the type test and the assertions of the block
 *                    child<ASTType>(ignoreChildren = true) {
 *
 *                        // In a "child" block, the tested node can be referred to as "it"
 *                        // Here, its static type is ASTType, so we can inspect properties
 *                        // of the node and make assertions
 *
 *                        it.typeImage shouldBe "int"
 *                        it.type shouldNotBe null
 *                    }
 *
 *                    // We don't care about that node, we only care that there is "some" node
 *                    unspecifiedChild()
 *                }
 *            }
 *
 *            // The subtree is ignored, but we check a ForUpdate is present at this child position
 *            child<ASTForUpdate>(ignoreChildren = true) {}
 *
 *            // Here, ignoreChildren is not specified and takes its default value of false.
 *            // The lambda has no "child" calls and the node will be asserted to have no children
 *            child<ASTBlock> {}
 *        }
 *    }
 *
 *    // To get good error messages, it's important to define assertions
 *    // on the node that is supposed to verify them, so if it needs some
 *    // value from its children, you can go fetch that value in two ways:
 *    // * if you just need the child node, the child method already returns that
 *    // * if you need some more complex value, or to return some subchild, use childRet
 *
 *    catchStmt should matchStmt<ASTCatchStatement> {
 *       it.isMulticatchStatement shouldBe true
 *
 *       // The childRet method is a variant of child which can return anything.
 *       // Specify the return type as a type parameter
 *       val types = childRet<ASTFormalParameter, List<ASTType>> {
 *
 *           // The child method returns the child (strongly typed)
 *           val ioe = child<ASTType>(ignoreChildren = true) {
 *               it.type shouldBe IOException::class.java
 *           }
 *
 *           val aerr = child<ASTType>(ignoreChildren = true) {
 *               it.type shouldBe java.lang.AssertionError::class.java
 *           }
 *
 *           unspecifiedChild()
 *
 *           // You have to use the annotated return type syntax
 *           return@childRet listOf(ioe, aerr)
 *       }
 *
 *       // Here you can use the returned value to perform more assertions*
 *
 *       it.caughtExceptionTypeNodes.shouldContainExactly(types)
 *       it.caughtExceptionTypes.shouldContainExactly(types.map { it.type })
 *
 *       it.exceptionName shouldBe "e"
 *
 *       child<ASTBlock> { }
 *    }
 */
inline fun <H : Any, reified N : H> baseMatchSubtree(adapter: TreeLikeAdapter<H>,
                                                     ignoreChildren: Boolean = false,
                                                     noinline nodeSpec: NWrapper<H, N>.() -> Unit): Matcher<H?> =
        verify {
            it shouldNotBe null
            NWrapper.executeWrapper(
                adapter,
                N::class.java,
                it!!,
                emptyList(),
                ignoreChildren,
                nodeSpec
            )
}