package org.veupathdb.lib.gradle.container.config;

import org.gradle.api.Task;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class GradleInputsOutputs {
  private static final byte TypeFiles = 0;
  private static final byte TypeDir   = 1;

  private final Object[] files;
  private final Object   dir;
  private final byte     type;

  private GradleInputsOutputs(@NotNull Object[] files) {
    this.files = files;
    this.dir   = null;
    this.type  = TypeFiles;
  }

  private GradleInputsOutputs(@NotNull Object file) {
    this.dir   = file;
    this.files = null;
    this.type  = TypeDir;
  }

  //
  //
  // Static Constructors
  //
  //

  @NotNull
  public static GradleInputsOutputs ofFiles(@NotNull String file) {
    return new GradleInputsOutputs(new Object[]{new File(file)});
  }

  @NotNull
  public static GradleInputsOutputs ofFiles(@NotNull File file) {
    return new GradleInputsOutputs(new Object[]{file});
  }

  @NotNull
  public static GradleInputsOutputs ofFiles(@NotNull File file1, @NotNull File file2) {
    return new GradleInputsOutputs(new Object[]{file1, file2});
  }

  @NotNull
  public static GradleInputsOutputs ofFiles(@NotNull File file1, @NotNull File file2, @NotNull File file3) {
    return new GradleInputsOutputs(new Object[]{file1, file2, file3});
  }

  @NotNull
  public static GradleInputsOutputs ofFiles(
    @NotNull File file1,
    @NotNull File file2,
    @NotNull File file3,
    @NotNull File file4
  ) {
    return new GradleInputsOutputs(new Object[]{file1, file2, file3, file4});
  }

  @NotNull
  public static GradleInputsOutputs ofDir(@NotNull File dir) {
    return new GradleInputsOutputs(dir);
  }

  //
  //
  // Implementation Methods
  //
  //

  @SuppressWarnings("ConstantConditions")
  public void applyInputs(@NotNull Task task) {
    switch (type) {
      case TypeFiles -> task.getInputs().files(files);
      case TypeDir -> task.getInputs().dir(dir);
      default -> throw new IllegalStateException("Unrecognized type.");
    }
  }

  @SuppressWarnings("ConstantConditions")
  public void applyOutputs(@NotNull Task task) {
    switch (type) {
      case TypeFiles -> task.getOutputs().files(files);
      case TypeDir -> task.getOutputs().dir(dir);
      default -> throw new IllegalStateException("Unrecognized type.");
    }
  }
}
