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
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SequentialIterator<T> implements Iterator<T> {
  public SequentialIterator(Iterator<? extends T>[] iters) {
    this.iters = iters.clone();
  }

  @SuppressWarnings("unchecked")
  public SequentialIterator(Iterator<? extends T> iter1, Iterator<? extends T> iter2) {
    this(new Iterator[] { iter1, iter2 });
  }

  @SuppressWarnings("unchecked")
  public SequentialIterator(
    Iterator<? extends T> iter1, Iterator<? extends T> iter2, Iterator<? extends T> iter3) {
    this(new Iterator[] { iter1, iter2, iter3 });
  }

  @SuppressWarnings("unchecked")
  public SequentialIterator(
    Iterator<? extends T> iter1,
    Iterator<? extends T> iter2,
    Iterator<? extends T> iter3,
    Iterator<? extends T> iter4) {
    this(new Iterator[] { iter1, iter2, iter3, iter4 });
  }

  private final Iterator<? extends T>[] iters;

  private int currentIter = 0;

  @Override
  public boolean hasNext() {
    if (currentIter >= iters.length) {
      return false;
    }
    else if (iters[currentIter].hasNext()) {
      return true;
    }
    else {
      currentIter++;
      return hasNext();
    }
  }

  @Override
  public T next() throws NoSuchElementException {
    if (hasNext()) {
      return iters[currentIter].next();
    }
    else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
