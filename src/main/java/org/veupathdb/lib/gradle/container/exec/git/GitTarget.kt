package org.veupathdb.lib.gradle.container.exec.git

/**
 * Git Target
 * <p>
 * Represents a tag, branch, or commit hash target for Git operations.
 *
 * @since 1.0.0
 */
@Suppress("unused")
fun interface GitTarget {

  companion object {
    /**
     * Default branch name for old style Git repositories.
     *
     * @since 2.0.0
     */
    val OldDefault: GitTarget = GitTarget { "master" }

    /**
     * Default branch name for old style Git repositories.
     *
     * @since 2.0.0
     */
    val NewDefault: GitTarget = GitTarget { "main" }

    /**
     * Returns a {@code GitTarget} instance wrapping the given target tag, branch,
     * or commit hash.
     *
     * @param gitTarget Target tag, branch, or commit hash.
     *
     * @return {@code GitTarget} instance wrapping the given value.
     *
     * @since 1.0.0
     */
    @JvmStatic
    fun of(gitTarget: String): GitTarget {
      if (NewDefault.equals(gitTarget))
        return NewDefault
      if (OldDefault.equals(gitTarget))
        return OldDefault

      //noinspection NullableProblems
      return GitTarget { gitTarget }
    }

    /**
     * Returns whether the given target name matches one of the default Git branch
     * names.
     *
     * @param target Target name to test.
     *
     * @return Whether the given target name matches one of the default Git branch
     * names.
     *
     * @since 2.0.0
     */
    @JvmStatic
    fun isDefault(target: String): Boolean {
      return NewDefault.equals(target) || OldDefault.equals(target)
    }
  }


  /**
   * Returns the name of this {@code GitTarget}.
   *
   * @return The name of this {@code GitTarget}.
   *
   * @since 1.0.0
   */
  fun name(): String

  /**
   * Returns whether this {@code GitTarget} is for a default Git branch.
   *
   * @return Whether this {@code GitTarget} is for a default Git branch.
   *
   * @since 1.0.0
   */
  fun isDefault(): Boolean {
    if (this == NewDefault || this == OldDefault)
      return true

    return isDefault(name())
  }

  /**
   * Returns whether this {@code GitTarget} is equal to the given other target.
   *
   * @param other Other {@code GitTarget} to compare to.
   *
   * @return Whether this {@code GitTarget} is equal to the given other target.
   *
   * @since 2.0.0
   */
  fun equals(other: GitTarget): Boolean {
    return this == other || this.name() == other.name()
  }

  /**
   * Returns whether this {@code GitTarget}'s name is equal to the given target
   * name.
   *
   * @param other Target name to compare to.
   *
   * @return Whether this {@code GitTarget}'s name is equal to the given target
   * name.
   *
   * @since 2.0.0
   */
  fun equals(other: String): Boolean {
    return this.name() == other
  }
}
