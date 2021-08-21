package org.veupathdb.lib.gradle.container.tasks.base.exec;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecConfiguration {
  private final List<@NotNull String>                 arguments   = new ArrayList<>();
  private final Map<@NotNull String, @NotNull String> environment = new HashMap<>();

  @NotNull
  public List<@NotNull String> getArguments() {
    return arguments;
  }

  public void setArguments(@NotNull List<@NotNull String> args) {
    arguments.addAll(args);
  }

  @NotNull
  public Map<@NotNull String, @NotNull String> getEnvironment() {
    return environment;
  }

  public void setEnvironment(@NotNull Map<@NotNull String, @NotNull String> env) {
    environment.putAll(env);
  }

  @Override
  public String toString() {
    return "ExecConfiguration{" +
      "arguments=" + arguments +
      ", environment=" + environment +
      '}';
  }
}
