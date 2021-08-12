package org.veupathdb.lib.gradle.container.util;

public class Counter {
  private int count;

  public void increment() {
    count++;
  }

  public void increment(Object ignored) {
    count++;
  }

  public int get() {
    return count;
  }
}
