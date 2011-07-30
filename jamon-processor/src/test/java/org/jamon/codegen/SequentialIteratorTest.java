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
