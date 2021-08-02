package org.veupathdb.lib.gradle.container.config;

@SuppressWarnings("unused")
public class Options {
  private String vendorDirectory;
  private String fgpUtilVersion;
  private String ramlForJaxRSVersion;
  private String binDirectory;

  public String getVendorDirectory() {
    return vendorDirectory;
  }

  public void setVendorDirectory(final String vendorDirectory) {
    this.vendorDirectory = vendorDirectory;
  }

  public String getFgpUtilVersion() {
    return fgpUtilVersion;
  }

  public void setFgpUtilVersion(String fgpUtilVersion) {
    this.fgpUtilVersion = fgpUtilVersion;
  }

  public String getRamlForJaxRSVersion() {
    return ramlForJaxRSVersion;
  }

  public void setRamlForJaxRSVersion(String ramlForJaxRSVersion) {
    this.ramlForJaxRSVersion = ramlForJaxRSVersion;
  }

  public String getBinDirectory() {
    return binDirectory;
  }

  public void setBinDirectory(String binDirectory) {
    this.binDirectory = binDirectory;
  }
}
