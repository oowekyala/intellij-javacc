/* Generated by JJTricks on Thu Jan 01 01:00:00 CET 1970 -- Not intended for manual editing. */

package org.exprs.ast;

public class ExBinaryExpression extends AbstractExExpression {

  public ExBinaryExpression(int id) {
    super(id);
  }

  public ExBinaryExpression(DummyExprParser parser, int id) {
    super(parser, id);
  }

  @Override
  public <T> void jjtAccept(DummyExprVisitorX<T> visitor, T data) {
    visitor.visit(this, data);
  }
}