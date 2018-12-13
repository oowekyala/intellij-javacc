package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree
import com.intellij.psi.PsiElement

/** An instance of [TreeLikeAdapter] for the [PsiElement] hierarchy. */
object InjectionTreeHierarchyAdapter : TreeLikeAdapter<InjectionStructureTree> {
    override fun getChild(node: InjectionStructureTree, i: Int): InjectionStructureTree = node.children[i]

    override fun numChildren(node: InjectionStructureTree): Int = node.children.size

    override fun nodeName(type: Class<out InjectionStructureTree>): String =
            type.simpleName.removeSurrounding("Jcc", "Impl")

    override fun childIndex(node: InjectionStructureTree): Int? = null

    override fun parent(node: InjectionStructureTree): InjectionStructureTree? = null
}


/**
 * Matcher for a node, using [NWrapper] to specify a subtree against which
 * the tested node will be tested.
 *
 * Use it with [io.kotlintest.should], e.g. `node should matchNode<ASTExpression> {}`.
 *
 * @param N Expected type of the node
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
inline fun <reified N : InjectionStructureTree> matchInjectionTree(ignoreChildren: Boolean = false,
                                                                   noinline nodeSpec: NWrapper<InjectionStructureTree, N>.() -> Unit) =
        baseMatchSubtree(InjectionTreeHierarchyAdapter, ignoreChildren, nodeSpec)