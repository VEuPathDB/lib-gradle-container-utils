package org.veupathdb.lib.gradle.container.tasks.base

import org.gradle.api.tasks.Internal
import java.io.File

abstract class BinInstallAction: Action() {

    /**
     * Returns a reference to the output bin directory.
     * <p>
     * This method is the same as {@link #getDependencyRoot()} just with a more
     * descriptive name.
     *
     * @return A reference to the output bin directory.
     *
     * @since 1.1.0
     */
    @Internal
    protected fun getBinRoot(): File =
        log.getter(File(RootDir, globalBuildConfiguration().binDirectory))

    protected fun globalBuildConfiguration() = options.binBuilds

}