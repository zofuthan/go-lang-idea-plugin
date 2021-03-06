package com.goide.completion;

import com.goide.GoConstants;
import com.goide.GoParserDefinition;
import com.goide.GoTypes;
import com.goide.psi.GoImportString;
import com.goide.psi.GoPackageClause;
import com.goide.util.GoUtil;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class GoCompletionContributor extends CompletionContributor {
  public GoCompletionContributor() {
    extend(CompletionType.BASIC, importString(), new GoImportPathsCompletionProvider());
  }

  @Override
  public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    PsiElement position = parameters.getPosition();
    if (position.getParent() instanceof GoPackageClause && position.getNode().getElementType() == GoTypes.IDENTIFIER) {
      PsiDirectory directory = parameters.getOriginalFile().getParent();
      Collection<String> packagesInDirectory = GoUtil.getAllPackagesInDirectory(directory);
      for (String packageName : packagesInDirectory) {
        result.addElement(LookupElementBuilder.create(packageName));
      }

      if (packagesInDirectory.isEmpty() && directory != null) {
        String packageFromDirectory = FileUtil.sanitizeFileName(directory.getName());
        if (!packageFromDirectory.isEmpty()) {
          result.addElement(LookupElementBuilder.create(packageFromDirectory));
        }
      }
      result.addElement(LookupElementBuilder.create(GoConstants.MAIN));
    }
    super.fillCompletionVariants(parameters, result);
  }

  private static PsiElementPattern.Capture<PsiElement> importString() {
    return PlatformPatterns.psiElement().withElementType(GoParserDefinition.STRING_LITERALS).withParent(GoImportString.class);
  }
}
