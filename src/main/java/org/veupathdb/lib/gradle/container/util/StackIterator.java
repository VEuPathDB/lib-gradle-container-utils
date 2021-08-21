package org.veupathdb.lib.gradle.container.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * Iterator Wrapping a {@link Stack} instance.
 *
 * @param <T> Generic type of this iterator and of the wrapped {@code Stack}
 *            instance.
 *
 * @since 1.1.0
 */
public record StackIterator<T>(
  @NotNull Stack<T> stack) implements Iterable<T>, Iterator<T>, Spliterator<T> {
  public StackIterator(@NotNull Stack<T> stack) {
    this.stack = stack;
  }

  ////  Iterable  ////////////////////////////////////////////////////////////////////////////////

  @Override
  @NotNull
  public Iterator<T> iterator() {
    return this;
  }

  @Override
  @NotNull
  public Spliterator<T> spliterator() {
    return this;
  }

  @Override
  public void forEach(@NotNull final Consumer<? super T> action) {
    while (!stack.isEmpty()) {
      action.accept(stack.pop());
    }
  }

  ////  Iterator  ////////////////////////////////////////////////////////////////////////////////

  @Override
  public boolean hasNext() {
    return !stack.empty();
  }

  @Override
  @NotNull
  public T next() {
    return stack.pop();
  }

  @Override
  public void forEachRemaining(@NotNull final Consumer<? super T> action) {
    while (!stack.isEmpty()) {
      action.accept(stack.pop());
    }
  }

  @Override
  public void remove() {}

  ////  Spliterator  /////////////////////////////////////////////////////////////////////////////

  @Override
  public boolean tryAdvance(Consumer<? super T> action) {
    if (stack.isEmpty()) {
      return false;
    }

    action.accept(stack.pop());
    return true;
  }

  @Override
  @Nullable
  public Spliterator<T> trySplit() {
    return null;
  }

  @Override
  public long estimateSize() {
    return stack.size();
  }

  @Override
  public int characteristics() {
    return ORDERED | SORTED | DISTINCT | NONNULL;
  }

  @Override
  public Comparator<? super T> getComparator() {
    return null;
  }
}
