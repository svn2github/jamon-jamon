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
