package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Vendor Clean Action
 * <p>
 * Base class for vendored dependency clean tasks.
 *
 * @since 1.1.0
 */
public abstract class VendorCleanAction extends CleanAction {
  @Override
  @Internal
  @NotNull
  protected File getTargetDirectory() {
    return log().getter(new File(RootDir, getOptions().getGlobalVendorConfig().getVendorDirectory()));
  }
}
