/* Generated by JJTricks on Thu Jan 01 01:00:00 CET 1970 -- Not intended for manual editing. */

package org.exprs.ast;

import org.exprs.ast.MyNodeParent;
import java.util.Stack;
import org.exprs.ast.NodeManipulator;

/**
 * This class is responsible for building the tree as the parser operates. Nodes are kept on a stack
 * and linked together when closed. Hooks defined on a {@link NodeManipulator manipulator} instance
 * allow interacting with {@link MyNodeParent} without forcing it to have a specific interface.
 */
public class JJTSimpleExprParserState {
  private final Stack<MyNodeParent> nodes = new Stack<MyNodeParent>();
  private final Stack<Integer> marks = new Stack<Integer>();
  private final NodeManipulator manipulator = new NodeManipulator();

  /**
   * Index of the first child of this node. If equal to {@link #nodes.size()}, no children are
   * pushed.
   */
  private int mk = 0;

  private boolean nodeCreated;

  /**
   * Returns true if the current node was closed and pushed (in case it was conditional). Reset as
   * soon as another node scope is opened.
   */
  public boolean nodeCreated() {
    return nodeCreated;
  }

  /**
   * Returns the root node of the AST. It only makes sense to call this after a successful parse.
   */
  public MyNodeParent rootNode() {
    return nodes.get(0);
  }

  /** Pushes a new node on to the stack. */
  public void pushNode(MyNodeParent n) {
    nodes.push(n);
  }

  /** Returns the node on the top of the stack, and remove it from the stack. */
  public MyNodeParent popNode() {
    if (nodes.size() - 1 < mk) {
      mk = marks.pop();
    }
    return nodes.pop();
  }

  /** Returns the node currently on the top of the stack. */
  public MyNodeParent peekNode() {
    return nodes.peek();
  }

  /** Returns the nth node on the stack. {@code peekNode(0) === peekNode()}. */
  public MyNodeParent peekNode(int n) {
    return nodes.get(nodes.size() - n - 1);
  }

  /** Returns the number of children on the stack in the current node scope. */
  public int nodeArity() {
    return nodes.size() - mk;
  }

  /** Abandon building the current node. */
  public void clearNodeScope(MyNodeParent n) {
    while (nodes.size() > mk) {
      popNode();
    }
    mk = marks.pop();
  }

  /** Start construction of the given node. */
  public void openNodeScope(MyNodeParent n) {
    marks.push(mk);
    mk = nodes.size();
    manipulator.onOpen(this, n);
  }

  /**
   * A definite node is constructed from a specified number of children. That number of nodes are
   * popped from the stack and made the children of the definite node. Then the definite node is
   * pushed on to the stack.
   */
  public void closeNodeScope(MyNodeParent n, int num) {
    mk = marks.pop();
    while (num-- > 0) {
      MyNodeParent c = popNode();
      manipulator.addChild(this, n, c, num);
    }
    manipulator.onPush(this, n);
    pushNode(n);
    nodeCreated = true;
  }

  /**
   * A conditional node is constructed if its condition is true. All the nodes that have been pushed
   * since the node was opened are made children of the conditional node, which is then pushed on to
   * the stack. If the condition is false the node is not constructed and they are left on the
   * stack.
   */
  public void closeNodeScope(MyNodeParent n, boolean condition) {
    if (condition) {
      int a = nodeArity();
      mk = marks.pop();
      while (a-- > 0) {
        MyNodeParent c = popNode();
        manipulator.addChild(this, n, c, a);
      }
      manipulator.onPush(this, n);
      pushNode(n);
      nodeCreated = true;
    } else {
      mk = marks.pop();
      nodeCreated = false;
    }
  }
}