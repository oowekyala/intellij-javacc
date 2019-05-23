// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.github.oowekyala.ijcc.lang.JccTypes.*;
import com.github.oowekyala.ijcc.lang.psi.stubs.JccScopedExpansionUnitStub;
import com.github.oowekyala.ijcc.lang.psi.*;
import com.intellij.psi.stubs.IStubElementType;

public class JccScopedExpansionUnitImpl extends JjtNodeClassOwnerImpl<JccScopedExpansionUnitStub> implements JccScopedExpansionUnit {

  public JccScopedExpansionUnitImpl(ASTNode node) {
    super(node);
  }

  public JccScopedExpansionUnitImpl(JccScopedExpansionUnitStub stub, IStubElementType stubType) {
    super(stub, stubType);
  }

  public void accept(@NotNull JccVisitor visitor) {
    visitor.visitScopedExpansionUnit(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JccVisitor) accept((JccVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public JccExpansionUnit getExpansionUnit() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, JccExpansionUnit.class));
  }

  @Override
  @NotNull
  public JccJjtreeNodeDescriptor getJjtreeNodeDescriptor() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, JccJjtreeNodeDescriptor.class));
  }

  @Override
  @Nullable
  public JccIdentifier getNameIdentifier() {
    JccJjtreeNodeDescriptor p1 = getJjtreeNodeDescriptor();
    return p1.getNameIdentifier();
  }

}
