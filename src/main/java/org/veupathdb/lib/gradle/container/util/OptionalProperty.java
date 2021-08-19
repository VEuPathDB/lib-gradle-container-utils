package org.veupathdb.lib.gradle.container.util;

import org.jetbrains.annotations.Nullable;

public class OptionalProperty {
  @Nullable
  private final String value;

  private final boolean set;

  private OptionalProperty(@Nullable final String value) {
    this.value = value;
    this.set = true;
  }

  private OptionalProperty() {
    this.value = null;
    this.set = false;
  }

  public boolean isSet() {
    return set;
  }

  public boolean isNull() {
    return value == null;
  }

  public @Nullable String getValue() {
    return value;
  }


}
