package org.veupathdb.lib.gradle.container.tasks.base;

import org.veupathdb.lib.gradle.container.config.ServiceProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Stack;

class Utils {
  static final String PropFileName = "service.properties";

  static ServiceProperties loadServiceProperties(final File projectRoot) throws IOException {
    final var props = new Properties();

    try (final var stream = new FileInputStream(new File(projectRoot, PropFileName))) {
      props.load(stream);
    }

    return new ServiceProperties(props);
  }

  static void deleteRecursive(final File target) {
    if (!target.isDirectory()) {
      if (!target.delete()) {
        throw new RuntimeException("Failed to delete file " + target);
      }

      return;
    }

    final var dirs = new Stack<File>();
    final var dels = new Stack<File>();

    dirs.push(target);
    dels.push(target);

    while (!dirs.empty()) {
      //noinspection ConstantConditions
      for (final var child : dirs.pop().listFiles()) {
        if (child.isDirectory()) {
          dirs.push(child);
          dels.push(child);
        } else {
          if (!child.delete()) {
            throw new RuntimeException("Failed to delete file " + child);
          }
        }
      }
    }

    while (!dels.empty()) {
      final var del = dels.pop();

      if (!del.delete()) {
        throw new RuntimeException("Failed to delete directory " + del);
      }
    }
  }
}
