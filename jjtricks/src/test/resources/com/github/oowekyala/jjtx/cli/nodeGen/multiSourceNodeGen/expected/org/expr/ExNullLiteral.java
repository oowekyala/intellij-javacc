/* Generated by JJTricks on Thu Jan 01 01:00:00 CET 1970 -- Not intended for manual editing. */

package org.expr;

public class ExNullLiteral extends ExLiteral {

  public ExNullLiteral(int id) {
    super(id);
  }

  public ExNullLiteral(DummyExprParser parser, int id) {
    super(parser, id);
  }

  @Override
  public Object jjtAccept(org.expr.DummyExprParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public void jjtAccept(org.expr.DummyExprVisitor visitor) {
    visitor.visit(this);
  }
}