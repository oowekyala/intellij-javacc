/* Generated by JJTricks on Thu Jan 01 01:00:00 CET 1970 -- Not intended for manual editing. */

package org.exprs.ast;

public class ExExpression extends AbstractMyNodeParent {

  public ExExpression(int id) {
    super(id);
  }

  public ExExpression(DummyExprParser parser, int id) {
    super(parser, id);
  }

  @Override
  public <T> void jjtAccept(DummyExprVisitorX<T> visitor, T data) {
    visitor.visit(this, data);
  }
}