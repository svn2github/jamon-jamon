/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2007 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

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
