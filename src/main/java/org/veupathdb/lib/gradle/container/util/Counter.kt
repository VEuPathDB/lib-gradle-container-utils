package org.veupathdb.lib.gradle.container.util

class Counter {
  private var count = 0

  fun increment() {
    count++
  }

  fun increment(ignored: Any) {
    count++
  }

  fun get() = count
}
