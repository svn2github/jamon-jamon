/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class SequentialList<T> extends AbstractList<T> {
  @SuppressWarnings("unchecked")
  private SequentialList(@SuppressWarnings("rawtypes") List[] lists) {
    this.lists = lists;
    this.size = totalSize(lists);
  }

  private int totalSize(List<?>[] lists) {
    int size = 0;
    for (List<?> list : lists) {
      size += list.size();
    }
    return size;
  }

  public SequentialList(List<? extends T> list1) {
    this(new List[] { list1 });
  }

  public SequentialList(List<? extends T> list1, List<? extends T> list2) {
    this(new List[] { list1, list2 });
  }

  public SequentialList(
    List<? extends T> list1, List<? extends T> list2, List<? extends T> list3) {
    this(new List[] { list1, list2, list3 });
  }

  private final List<? extends T>[] lists;

  private final int size;

  @Override
  public T get(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException();
    }
    int listIndex = index;
    for (List<? extends T> list : lists) {
      if (listIndex >= list.size()) {
        listIndex -= list.size();
      }
      else {
        return list.get(listIndex);
      }
    }
    throw new IndexOutOfBoundsException();
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Iterator<T> iterator() {
    @SuppressWarnings("unchecked")
    Iterator<? extends T>[] iters = new Iterator[lists.length];
    for (int i = 0; i < lists.length; i++) {
      iters[i] = lists[i].iterator();
    }
    return new SequentialIterator<T>(iters);
  }

}
