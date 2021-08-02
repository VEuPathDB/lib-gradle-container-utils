package org.veupathdb.lib.gradle.container.tasks.base;

import org.veupathdb.lib.gradle.container.config.ServiceProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

class Utils {
  static final String PropFileName = "service.properties";

  static ServiceProperties loadServiceProperties(final File projectRoot) throws IOException {
    final var props = new Properties();

    try (final var stream = new FileInputStream(new File(projectRoot, PropFileName))) {
      props.load(stream);
    }

    return new ServiceProperties(props);
  }
}
