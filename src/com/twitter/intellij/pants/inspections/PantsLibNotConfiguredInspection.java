package com.twitter.intellij.pants.inspections;

import com.intellij.codeInspection.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.LibraryDependencyScopeSuggester;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.twitter.intellij.pants.PantsBundle;
import com.twitter.intellij.pants.util.PantsUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PantsLibNotConfiguredInspection extends LocalInspectionTool {
  @NotNull
  public String getGroupDisplayName() {
    return PantsBundle.message("inspections.group.name");
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return PantsBundle.message("pants.inspection.library.configured");
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public String getShortName() {
    return "PantsLibNotConfigured";
  }

  @Nullable
  @Override
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (!PantsUtil.BUILD.equals(file.getName())) {
      return ProblemDescriptor.EMPTY_ARRAY;
    }
    final Project project = file.getProject();

    final Module module = ModuleUtil.findModuleForPsiElement(file);
    if (module == null) {
      return ProblemDescriptor.EMPTY_ARRAY;
    }

    if (LibraryUtil.findLibrary(module, PantsUtil.PANTS_LIBRARY_NAME) != null) {
      return ProblemDescriptor.EMPTY_ARRAY;
    }

    final LibraryTable libraryTable = ProjectLibraryTable.getInstance(project);
    final Library libraryByName = libraryTable.getLibraryByName(PantsUtil.PANTS_LIBRARY_NAME);
    if (libraryByName == null) {
      // skip util project lib is configured
      return ProblemDescriptor.EMPTY_ARRAY;
    }

    final ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
      file,
      PantsBundle.message("pants.inspection.library.not.configured", module.getName()),
      isOnTheFly,
      new LocalQuickFix[]{new ConfigureLibFix(module)},
      ProblemHighlightType.GENERIC_ERROR
    );
    return new ProblemDescriptor[]{problemDescriptor};
  }

  public static class ConfigureLibFix implements LocalQuickFix {
    @NotNull
    private final Module myModule;

    public ConfigureLibFix(@NotNull Module module) {
      myModule = module;
    }

    @NotNull
    @Override
    public String getName() {
      return PantsBundle.message("pants.inspection.fix.it");
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      applyFix(project, myModule);
    }

    public static void applyFix(@NotNull Project project, @NotNull Module myModule) {
      final LibraryTable libraryTable = ProjectLibraryTable.getInstance(project);
      final Library pantsLib = libraryTable.getLibraryByName(PantsUtil.PANTS_LIBRARY_NAME);
      if (pantsLib == null) {
        // not possible
        Messages.showErrorDialog(
          project,
          PantsBundle.message("pants.inspection.library.not.found"),
          PantsBundle.message("pants.error.title")
        );
        return;
      }

      ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(myModule).getModifiableModel();
      DependencyScope defaultScope = LibraryDependencyScopeSuggester.getDefaultScope(pantsLib);
      modifiableModel.addLibraryEntry(pantsLib).setScope(defaultScope);
      modifiableModel.commit();
    }
  }
}
