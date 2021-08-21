package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Bin Clean Action
 * <p>
 * Base class for build/bin dependency clean tasks.
 *
 * @since 1.1.0
 */
public abstract class BinCleanAction extends CleanAction {
  @Override
  @Internal
  @NotNull
  protected File getTargetDirectory() {
    return log().getter(new File(RootDir, getOptions().getGlobalBinConfig().getBinDirectory()));
  }
}
