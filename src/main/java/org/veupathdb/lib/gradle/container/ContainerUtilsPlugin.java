package org.veupathdb.lib.gradle.container;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.veupathdb.lib.gradle.container.tasks.InstallFgpUtil;
import org.veupathdb.lib.gradle.container.tasks.UninstallFgpUtil;
import org.veupathdb.lib.gradle.container.tasks.VendorAction;

public class ContainerUtilsPlugin implements Plugin<Project> {
  public static final String ExtensionName = "BuildOptions";

  public static class Options {
    private String vendorDirectory = VendorAction.DefaultVendorDir;

    public String getVendorDirectory() {
      return vendorDirectory;
    }

    public void setVendorDirectory(final String vendorDirectory) {
      if (vendorDirectory == null)
        throw new NullPointerException();

      this.vendorDirectory = vendorDirectory;
    }

    public boolean isDefaultVendorDirectory() {
      return VendorAction.DefaultVendorDir.equals(vendorDirectory);
    }
  }

  public void apply(final Project project) {
    // Register Global Options
    project.getExtensions().create(ExtensionName, Options.class);

    // Register Tasks
    project.getTasks().create("fgputilInstall", InstallFgpUtil::init);
    project.getTasks().create("fgputilUninstall", UninstallFgpUtil::init);
  }
}
