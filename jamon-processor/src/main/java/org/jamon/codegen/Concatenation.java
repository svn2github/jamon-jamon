/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

class Concatenation<T> extends AbstractCollection<T> {
  private final Collection<? extends T>[] collections;

  private final int size;

  private Concatenation(Collection<? extends T>... collections) {
    this.collections = collections;
    size = totalSize(collections);
  }

  private static int totalSize(Collection<?>... collections) {
    int size = 0;
    for (Collection<?> collection : collections) {
      size += collection.size();
    }
    return size;
  }

  @SuppressWarnings("unchecked")
  public Concatenation(Collection<? extends T> collection1, Collection<? extends T> collection2) {
    this(new Collection[] { collection1, collection2 });
  }

  @SuppressWarnings("unchecked")
  public Concatenation(
    Collection<? extends T> collection1,
    Collection<? extends T> collection2,
    Collection<? extends T> collection3) {
    this(new Collection[] { collection1, collection2, collection3 });
  }

  @Override
  public Iterator<T> iterator() {
    @SuppressWarnings("unchecked")
    Iterator<? extends T>[] iters = new Iterator[collections.length];
    for (int i = 0; i < collections.length; i++) {
      iters[i] = collections[i].iterator();
    }
    return new SequentialIterator<T>(iters);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean contains(Object o) {
    for (Collection<? extends T> collection : collections) {
      if (collection.contains(o)) {
        return true;
      }
    }
    return false;
  }
}
