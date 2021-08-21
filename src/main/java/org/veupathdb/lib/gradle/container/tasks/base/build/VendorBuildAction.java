package org.veupathdb.lib.gradle.container.tasks.base.build;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

/**
 * Vendor Build Action
 * <p>
 * Base class for vendored dependency build tasks.
 *
 * @since 1.1.0
 */
public abstract class VendorBuildAction extends BuildAction {
  @Override
  @Internal
  @NotNull
  protected File getDependencyRoot() {
    return log().getter(new File(RootDir, globalBuildConfiguration().getVendorDirectory()));
  }

  @Override
  protected GlobalVendorBuildConfiguration globalBuildConfiguration() {
    return getOptions().getGlobalVendorConfig();
  }
}
