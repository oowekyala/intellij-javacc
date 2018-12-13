package com.github.oowekyala.ijcc.lang.util

import arrow.core.Either
import io.kotlintest.Matcher
import io.kotlintest.Result
import kotlin.test.assertFalse
import kotlin.test.assertTrue


/**
 * Wraps a node, providing easy access to [it]. Additional matching
 * methods are provided to match children. This DSL supports objects
 * of any type provided they can be viewed as nodes of a tree. The
 * [TreeLikeAdapter] type class witnesses this property.
 *
 *
 * @property it         Wrapped node
 * @param adapter       Instance of the [TreeLikeAdapter] type class for the [H] type
 * @param matcherPath   List of types of the parents of this node, used to reconstruct a path for error messages
 * @param childMatchersAreIgnored Ignore calls to [child]
 *
 * @param H Hierarchy of the node
 * @param N Type of the node
 */
class NWrapper<H : Any, N : H> private constructor(val it: N,
                                                   private val adapter: TreeLikeAdapter<H>,
                                                   private val matcherPath: List<Class<out H>>,
                                                   private val childMatchersAreIgnored: Boolean) {

    /** Index to which the next child matcher will apply. */
    private var nextChildMatcherIdx = 0

    private fun shiftChild(num: Int = 1): H {

        checkChildExists(nextChildMatcherIdx)

        val ret = adapter.getChild(it, nextChildMatcherIdx)

        nextChildMatcherIdx += num
        return ret
    }


    private fun checkChildExists(childIdx: Int) =
            assertTrue(
                formatErrorMessage(
                    adapter,
                    matcherPath,
                    "Node has fewer children than expected, child #$childIdx doesn't exist"
                )
            ) {
                childIdx < adapter.numChildren(it)
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
            return executeWrapper(adapter, childType, shiftChild(), matcherPath, ignoreChildren, nodeSpec)
        else
            throw IllegalStateException(
                formatErrorMessage(
                    adapter,
                    matcherPath,
                    "Calling child when ignoreChildren=true is forbidden"
                )
            )
    }


    override fun toString(): String {
        return "NWrapper<${adapter.nodeName(it::class.java)}>"
    }


    companion object {


        private fun <H : Any> formatPath(adapter: TreeLikeAdapter<H>, matcherPath: List<Class<out H>>) =
                when {
                    matcherPath.isEmpty() -> "<root>"
                    else                  -> matcherPath.joinToString(separator = "/", prefix = "/") {
                        adapter.nodeName(
                            it
                        )
                    }
                }

        private fun <H : Any> formatErrorMessage(adapter: TreeLikeAdapter<H>,
                                                 matcherPath: List<Class<out H>>,
                                                 message: String) =
                "At ${formatPath(adapter, matcherPath)}: $message"

        /**
         * Execute wrapper assertions on a node.
         *
         * @param childType Expected type of [toWrap]
         * @param toWrap Node on which to execute the assertions
         * @param matcherPath List of types of the parents of this node, used to reconstruct a path for error messages
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
                                                        matcherPath: List<Class<out H>>,
                                                        ignoreChildrenMatchers: Boolean,
                                                        spec: NWrapper<H, M>.() -> R): R {

            val nodeNameForMsg = when {
                matcherPath.isEmpty() -> "node"
                else                  -> "child #${adapter.childIndex(toWrap)}"
            }

            assertTrue(
                formatErrorMessage(
                    adapter,
                    matcherPath,
                    "Expected $nodeNameForMsg to have type ${adapter.nodeName(childType)}, actual ${adapter.nodeName(
                        toWrap.javaClass
                    )}"
                )
            ) {
                childType.isInstance(toWrap)
            }

            val childPath = matcherPath + childType
            @Suppress("UNCHECKED_CAST")
            val m = toWrap as M

            val wrapper = NWrapper(m, adapter, childPath, ignoreChildrenMatchers)

            val ret: R = try {
                wrapper.spec()
            } catch (e: AssertionError) {
                if (e.message?.matches("At (/.*?|<root>):.*".toRegex()) == false) {
                    // the exception has no path, let's add one
                    throw AssertionError(
                        formatErrorMessage(
                            adapter, childPath, e.message
                                ?: "No explanation provided"
                        ), e
                    )
                }
                throw e
            }

            assertFalse(
                formatErrorMessage(
                    adapter,
                    childPath,
                    "Wrong number of children, expected ${wrapper.nextChildMatcherIdx}, actual ${adapter.numChildren(
                        wrapper.it
                    )}"
                )
            ) {
                !ignoreChildrenMatchers && wrapper.nextChildMatcherIdx != adapter.numChildren(wrapper.it)
            }
            return ret
        }
    }
}

/** Type class for something that can behave as a node in a tree. */
interface TreeLikeAdapter<N : Any> {

    /** Gets the ith child of the given object. */
    fun getChild(node: N, i: Int): N

    /** Gets the number of children the node has. */
    fun numChildren(node: N): Int

    /** Gets the display name of this type of node. */
    fun nodeName(type: Class<out N>): String

    /**
     * Gets the index of this node among the children of its parent.
     * This is to provide better error messages. If the child doesn't have
     * access to its parents, then do return null.
     */
    fun childIndex(node: N): Int?

    /** Gets the parent of this node. */
    fun parent(node: N): N?

}

/**
 * Base method to produce a kotlintest [Matcher] for nodes of a hierarchy [H]. The [H] type
 * parameter and the [adapter] are unnecessary when the domain is known, so concrete DSLs
 * should provide their own method that delegates to this one and provides the adapter.
 */
inline fun <H : Any, reified N : H> baseMatchSubtree(adapter: TreeLikeAdapter<H>,
                                                     ignoreChildren: Boolean = false,
                                                     noinline nodeSpec: NWrapper<H, N>.() -> Unit) = object :
    Matcher<H?> {
    override fun test(value: H?): Result {
        if (value == null) {
            return Result(false, "Expecting the node not to be null", "")
        }

        val matchRes = try {
            Either.Right(NWrapper.executeWrapper(adapter, N::class.java, value, emptyList(), ignoreChildren, nodeSpec))
        } catch (e: AssertionError) {
            Either.Left(e)
        }

        val didMatch = matchRes.isRight()

        // Output when the node should have matched and did not
        //
        val failureMessage: String = matchRes.fold({
            // Here the node failed
            it.message ?: "The node did not match the pattern (no cause specified)"
        }, {
            // The node matched, which was expected
            "SHOULD NOT BE OUTPUT"
        })

        val negatedMessage = matchRes.fold({
            // the node didn't match, which was expected
            "SHOULD NOT BE OUTPUT"
        }, {
            "The node should not have matched this pattern"
        })


        return Result(didMatch, failureMessage, negatedMessage)
    }
}