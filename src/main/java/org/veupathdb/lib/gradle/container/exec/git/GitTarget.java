package org.veupathdb.lib.gradle.container.exec.git;

import org.jetbrains.annotations.NotNull;

/**
 * Git Target
 * <p>
 * Represents a tag, branch, or commit hash target for Git operations.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface GitTarget {

  /**
   * Default branch name for old style Git repositories.
   *
   * @since 2.0.0
   */
  @NotNull
  @SuppressWarnings("NullableProblems")
  GitTarget OldDefault = "master"::toString;

  /**
   * Default branch name for old style Git repositories.
   *
   * @since 2.0.0
   */
  @NotNull
  @SuppressWarnings("NullableProblems")
  GitTarget NewDefault = "main"::toString;

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
  @NotNull
  static GitTarget of(@NotNull final String gitTarget) {
    if (NewDefault.equals(gitTarget))
      return NewDefault;
    if (OldDefault.equals(gitTarget))
      return OldDefault;

    //noinspection NullableProblems
    return gitTarget::toString;
  }

  /**
   * Returns the name of this {@code GitTarget}.
   *
   * @return The name of this {@code GitTarget}.
   *
   * @since 1.0.0
   */
  @NotNull
  String name();

  /**
   * Returns whether this {@code GitTarget} is for a default Git branch.
   *
   * @return Whether this {@code GitTarget} is for a default Git branch.
   *
   * @since 1.0.0
   */
  default boolean isDefault() {
    if (this == NewDefault || this == OldDefault)
      return true;

    return isDefault(name());
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
  default boolean equals(@NotNull final GitTarget other) {
    return this == other || this.name().equals(other.name());
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
  default boolean equals(@NotNull final String other) {
    return this.name().equals(other);
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
  static boolean isDefault(@NotNull final String target) {
    return NewDefault.equals(target) || OldDefault.equals(target);
  }
}
