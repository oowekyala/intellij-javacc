/*
 * Generated by JJTricks at Thu Jan 01 01:00:00 CET 1970
 * Not intended for manual editing.
 */

package com.jjtx.exprs;

import com.jjtx.exprs.ASTNode;
import java.util.Stack;
import com.jjtx.exprs.JJTSimpleExprParserState;

public final class NodeManipulator {

  public static final NodeManipulator DEFAULT_INSTANCE = new NodeManipulator();

  /**
   * Called when a node is first open. In this state, the node has no children yet, and no parent.
   * The default implementation calls {@code jjtOpen}.
   */
  public void onOpen(JJTSimpleExprParserState builder, ASTNode node) {
    node.jjtOpen();
  }

  /**
   * Called when a node is done being built. In this state, the node already has all its children,
   * but no parent yet. It's not yet on the stack of the tree builder.
   *
   * <p>The default implementation calls {@code jjtClose}.
   */
  public void onPush(JJTSimpleExprParserState builder, ASTNode node) {
    node.jjtClose();
  }

  /**
   * Called when a node is done being built. In this state, the node already has all its children,
   * but no parent yet. The default implementation calls {@code jjtClose}.
   */
  public void addChild(JJTSimpleExprParserState builder, ASTNode parent, ASTNode child, int index) {
    child.jjtSetParent(child);
    parent.jjtAddChild(child, index);
  }
}