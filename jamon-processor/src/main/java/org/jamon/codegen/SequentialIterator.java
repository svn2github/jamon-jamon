/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
