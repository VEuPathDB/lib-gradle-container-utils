package org.veupathdb.lib.gradle.container.util

import java.util.Spliterator
import java.util.Spliterator.*
import java.util.Stack
import java.util.function.Consumer


/**
 * Iterator Wrapping a [Stack] instance.
 *
 * @param T Generic type of this iterator and of the wrapped [Stack] instance.
 *
 * @since 1.1.0
 */
data class StackIterator<T>(val stack: Stack<T>)
  : Iterable<T>
  , Iterator<T>
  , Spliterator<T>
{

  ////  Iterable  ////////////////////////////////////////////////////////////////////////////////

  override fun iterator() = this

  override fun spliterator() = this

  override fun forEach(action: Consumer<in T>) {
    while (!stack.isEmpty()) {
      action.accept(stack.pop())
    }
  }

  ////  Iterator  ////////////////////////////////////////////////////////////////////////////////

  override fun hasNext() = !stack.empty()

  override fun next(): T = stack.pop()

  override fun forEachRemaining(action: Consumer<in T>) {
    while (!stack.isEmpty()) {
      action.accept(stack.pop())
    }
  }

  ////  Spliterator  /////////////////////////////////////////////////////////////////////////////

  override fun tryAdvance(action: Consumer<in T>): Boolean {
    if (stack.isEmpty()) {
      return false
    }

    action.accept(stack.pop())
    return true
  }

  override fun trySplit(): Spliterator<T>? {
    return null
  }

  override fun estimateSize(): Long {
    return stack.size.toLong()
  }

  override fun characteristics(): Int {
    return ORDERED or SORTED or DISTINCT or NONNULL
  }

  override fun getComparator(): Comparator<in T>? {
    return null
  }
}
