package org.veupathdb.lib.gradle.container.tasks.base.build;


import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Bin Build Action
 * <p>
 * Base class for build/bin dependency build tasks.
 *
 * @since 1.1.0
 */
public abstract class BinBuildAction extends BuildAction {

  /**
   * Returns a reference to the output bin directory.
   * <p>
   * This method is the same as {@link #getDependencyRoot()} just with a more
   * descriptive name.
   *
   * @return A reference to the output bin directory.
   *
   * @since 1.1.0
   */
  @Internal
  @NotNull
  protected File getBinRoot() {
    return log().getter(new File(RootDir, globalBuildConfiguration().getBinDirectory()));
  }

  @Override
  @Internal
  @NotNull
  protected File getDependencyRoot() {
    return getBinRoot();
  }

  @Override
  protected GlobalBinBuildConfiguration globalBuildConfiguration() {
    return getOptions().getGlobalBinConfig();
  }
}
