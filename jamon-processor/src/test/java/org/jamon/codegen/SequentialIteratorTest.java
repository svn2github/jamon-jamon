/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import junit.framework.TestCase;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SequentialIteratorTest extends TestCase {
  public void testNoIters() {
    assertIterEquals(constructIterator(new String[0]), new SequentialIterator<Object>(
        new Iterator<?>[0]));
  }

  public void testOneEmptyIter() {
    Iterator<String> i1 = constructIterator(new String[0]);
    assertIterEquals(i1, new SequentialIterator<Object>(new Iterator<?>[] { i1 }));
  }

  public void testOneIter() {
    Iterator<String> i1 = constructIterator(new String[] { "one", "two" });
    assertIterEquals(
      i1,
      new SequentialIterator<Object>(new Iterator<?>[] {
          constructIterator(new String[] { "one", "two" }) }));
  }

  public void testTwoIters() {
    Iterator<String> i1 = constructIterator(new String[] { "one", "two" });
    Iterator<String> i2 = constructIterator(new String[] { "three", "four" });
    Iterator<String> combined = constructIterator(new String[] { "one", "two", "three", "four" });
    assertIterEquals(combined, new SequentialIterator<String>(i1, i2));
  }

  public void testTwoItersFirstEmpty() {
    Iterator<String> i1 = constructIterator(new String[0]);
    Iterator<String> i2 = constructIterator(new String[] { "three", "four" });
    Iterator<String> combined = constructIterator(new String[] { "three", "four" });
    assertIterEquals(combined, new SequentialIterator<String>(i1, i2));
  }

  public void testTwoItersSecondEmpty() {
    Iterator<String> i1 = constructIterator(new String[] { "one", "two" });
    Iterator<String> i2 = constructIterator(new String[0]);
    Iterator<String> combined = constructIterator(new String[] { "one", "two" });
    assertIterEquals(combined, new SequentialIterator<String>(i1, i2));
  }

  public void testThreeItersSecondEmpty() {
    Iterator<String> i1 = constructIterator(new String[] { "one", "two" });
    Iterator<String> i2 = constructIterator(new String[0]);
    Iterator<String> i3 = constructIterator(new String[] { "three", "four" });
    Iterator<String> combined = constructIterator(new String[] { "one", "two", "three", "four" });
    assertIterEquals(combined, new SequentialIterator<String>(i1, i2, i3));
  }

  private static Iterator<String> constructIterator(String[] objs) {
    return Arrays.asList(objs).iterator();
  }

  private void assertIterEquals(
    Iterator<? extends Object> expected, Iterator<? extends Object> actual) {
    while (expected.hasNext()) {
      assertTrue(actual.hasNext());
      assertEquals(expected.next(), actual.next());
    }
    assertTrue(!actual.hasNext());
    try {
      actual.next();
      fail("next should have thrown an exception");
    }
    catch (NoSuchElementException e) {}
  }
}
