package com.twitter.intellij.pants.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.twitter.intellij.pants.util.PantsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * todo: remove dirty hack after PyPreferenceCompletionProvider patch is merged in IntelliJ
 */
public class PantsCompletionContributor extends CompletionContributor {
  /*
  TODO: un-hardcode target list
  aliases from TestData/userHome/.pants.d/bin/pants.pex/.deps/pantsbuild.pants-0.0.17-py2-none-any.whl/pants/base/build_file_aliases
   */
  private static List<String> aliases = Arrays.asList(
    "annotation_processor",
    "artifact",
    "artifact",
    "bundle",
    "credentials",
    "dependencies",
    "egg",
    "exclude",
    "fancy_pants",
    "jar",
    "java_agent",
    "java_library",
    "java_antlr_library",
    "java_protobuf_library",
    "junit_tests",
    "java_tests",
    "java_thrift_library",
    "jvm_binary",
    "jvm_app",
    "page",
    "python_artifact",
    "python_binary",
    "python_library",
    "python_antlr_library",
    "python_requirement",
    "python_thrift_library",
    "python_test",
    "python_test_suite",
    "repo",
    "resources",
    "scala_library",
    "scala_specs",
    "scalac_plugin",
    "source_root",
    "wiki"
  );

  public PantsCompletionContributor() {
    extend(
      CompletionType.BASIC,
      psiElement().withParent(PyReferenceExpression.class),
      new CompletionProvider<CompletionParameters>() {
        @Override
        protected void addCompletions(
          @NotNull CompletionParameters parameters,
          ProcessingContext context,
          @NotNull CompletionResultSet result
        ) {
          final PsiFile psiFile = parameters.getOriginalFile();
          if (!PantsUtil.BUILD.equals(psiFile.getName())) {
            return;
          }
          for (String alias : aliases) {
            result.addElement(LookupElementBuilder.create(alias));
          }
        }
      }
    );
  }
}
