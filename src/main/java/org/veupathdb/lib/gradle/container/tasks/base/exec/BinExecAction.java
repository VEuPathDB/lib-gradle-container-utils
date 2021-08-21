package org.veupathdb.lib.gradle.container.tasks.base.exec;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Bin Exec Action
 * <p>
 * Base class for build/bin dependency execution tasks.
 *
 * @since 1.1.0
 */
public abstract class BinExecAction extends ExecAction {
  @Override
  @Internal
  @NotNull
  protected File getWorkDirectory() {
    return log().getter(new File(RootDir, getOptions().getGlobalBinConfig().getBinDirectory()));
  }
}
