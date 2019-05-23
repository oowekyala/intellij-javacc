// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.github.oowekyala.ijcc.lang.psi.stubs.JccScopedExpansionUnitStub;

public interface JccScopedExpansionUnit extends JccIdentifierOwner, JccExpansionUnit, JjtNodeClassOwner, StubBasedPsiElement<JccScopedExpansionUnitStub> {

  @NotNull
  JccExpansionUnit getExpansionUnit();

  @NotNull
  JccJjtreeNodeDescriptor getJjtreeNodeDescriptor();

  @Nullable
  JccIdentifier getNameIdentifier();

}
