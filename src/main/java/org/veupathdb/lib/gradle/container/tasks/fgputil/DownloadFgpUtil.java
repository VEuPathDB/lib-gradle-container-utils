package org.veupathdb.lib.gradle.container.tasks.fgputil;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.Action;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collection;

public class DownloadFgpUtil extends Action {

  public static final String TaskName = "download-fgputil";

  private static final String ReleaseUrl = "https://github.com/VEuPathDB/FgpUtil/releases/%s";

  private String tag;

  private File vendorDir;

  private void init() {
    log().info("Fetching download URL");

    if (vendorDir == null) {
      vendorDir = new File(RootDir, "vendor");
      vendorDir.mkdir();
    }

    if (tag == null) {
      tag = opts().getVersion();
    }

    if (tag.equals("latest")) {
      log().info("Determining real git tag.");

      tag = getRealTag();
    }
  }

  @Override
  public @NotNull String pluginDescription() {
    return "Downloads the selected (or latest) version of FgpUtil";
  }

  @Override
  public void fillIncrementalOutputFiles(@NotNull Collection<File> files) {
    init();

    Arrays.stream(opts().getTargets())
      .map(name -> new File(vendorDir, name))
      .forEach(files::add);
  }

  @Override
  public void execute() {
    init();

    for (var i : opts().getTargets()) {
      var url = String.format(ReleaseUrl, "download/" + tag + "/" + i);

      try {

        var res = HttpClient.newHttpClient().send(
          HttpRequest.newBuilder(URI.create(url))
            .GET()
            .build(),
          HttpResponse.BodyHandlers.ofInputStream()
        );

        var file = new File(vendorDir, i);
        file.delete();
        file.createNewFile();

        try (var stream = new FileOutputStream(file)) {
          res.body().transferTo(stream);
        }

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private FgpUtilConfiguration opts() {
    return getOptions().getFgpUtilConfig();
  }

  private String getRealTag() {
    try {
      var res = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER)
        .build()
        .send(
          HttpRequest.newBuilder(URI.create(String.format(ReleaseUrl, "latest")))
            .GET()
            .build(),
          HttpResponse.BodyHandlers.discarding()
        );

      var location = res.headers().map().get("location").get(0);

      return location.substring(location.lastIndexOf('/') + 1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
