/* Generated by JJTricks on Thu Jan 01 01:00:00 CET 1970 -- Not intended for manual editing. */

package org.expr;

public interface DummyExprVisitor {

  /** Visits {@linkplain Root Root}. This is the root of the delegation chain. */
  default void visit(Root node) {
    node.childrenAccept(this);
  }

  /**
   * Visits {@linkplain ExExpression Expression}. Delegates to {@link #visit(Root) } if
   * unimplemented.
   *
   * <p>This method is delegated to by:
   *
   * <ul>
   *   <li>{@link #visit(ExBinaryExpression) }
   *   <li>{@link #visit(ExLiteral) }
   * </ul>
   */
  default void visit(ExExpression node) {
    visit((Root) node);
  }

  /**
   * Visits {@linkplain ExBinaryExpression BinaryExpression}. Delegates to {@link
   * #visit(ExExpression) } if unimplemented.
   */
  default void visit(ExBinaryExpression node) {
    visit((ExExpression) node);
  }

  /**
   * Visits {@linkplain ExLiteral Literal}. Delegates to {@link #visit(ExExpression) } if
   * unimplemented.
   *
   * <p>This method is delegated to by:
   *
   * <ul>
   *   <li>{@link #visit(ExNullLiteral) }
   *   <li>{@link #visit(ExIntegerLiteral) }
   * </ul>
   */
  default void visit(ExLiteral node) {
    visit((ExExpression) node);
  }

  /**
   * Visits {@linkplain ExNullLiteral NullLiteral}. Delegates to {@link #visit(ExLiteral) } if
   * unimplemented.
   */
  default void visit(ExNullLiteral node) {
    visit((ExLiteral) node);
  }

  /**
   * Visits {@linkplain ExIntegerLiteral IntegerLiteral}. Delegates to {@link #visit(ExLiteral) } if
   * unimplemented.
   */
  default void visit(ExIntegerLiteral node) {
    visit((ExLiteral) node);
  }
}
