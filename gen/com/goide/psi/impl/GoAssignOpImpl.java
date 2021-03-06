// This is a generated file. Not intended for manual editing.
package com.goide.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.goide.GoTypes.*;
import com.goide.psi.*;

public class GoAssignOpImpl extends GoCompositeElementImpl implements GoAssignOp {

  public GoAssignOpImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) ((GoVisitor)visitor).visitAssignOp(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getAssign() {
    return findChildByType(ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getBitAndAssign() {
    return findChildByType(BIT_AND_ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getBitClearAssign() {
    return findChildByType(BIT_CLEAR_ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getBitOrAssign() {
    return findChildByType(BIT_OR_ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getBitXorAssign() {
    return findChildByType(BIT_XOR_ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getMinusAssign() {
    return findChildByType(MINUS_ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getMulAssign() {
    return findChildByType(MUL_ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getPlusAssign() {
    return findChildByType(PLUS_ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getQuotientAssign() {
    return findChildByType(QUOTIENT_ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getRemainderAssign() {
    return findChildByType(REMAINDER_ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getShiftLeftAssign() {
    return findChildByType(SHIFT_LEFT_ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getShiftRightAssign() {
    return findChildByType(SHIFT_RIGHT_ASSIGN);
  }

}
