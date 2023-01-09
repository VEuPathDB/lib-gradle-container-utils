package org.veupathdb.lib.gradle.container.tasks.check

import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.veupathdb.lib.gradle.container.tasks.base.Action
import java.nio.file.Paths

/**
 * This task creates a temporary '.dependencies' directory and asks Gradle to copy
 * the files in the compile classpath into it.  This triggers a download of all
 * declared dependencies, which are cached in the gradle cache before being copied
 * into the temporary directory.  This task then deletes the temporary directory,
 * leaving the downloaded dependencies in the gradle cache.
 *
 * This allows the download of dependencies to be done in advance of a compile step,
 * which, when called as a separate step in a Dockerfile, can optimizing docker cache
 * hits since depencencies change more rarely than project source code.
 */
open class DownloadDependencies : Copy() {

    companion object {
        const val TaskName = "download-dependencies"

        @JvmStatic
        fun init(action: DownloadDependencies) {
            action.register();
        }
    }

    fun register() {
        description = "Downloads this projects dependencies, adding them to the gradle cache"
        group = Action.Group
        actions.add { execute() }
    }

    private val dependencyDir = Paths.get(".dependencies")

    fun execute() {
        project.mkdir(dependencyDir)
        val ext = project.extensions.getByType(JavaPluginExtension::class.java);
        val dependencies = ext.sourceSets.findByName("main")?.compileClasspath;
        from(dependencies)
        into(dependencyDir)
        copy()
        project.delete(dependencyDir)
    }
}
