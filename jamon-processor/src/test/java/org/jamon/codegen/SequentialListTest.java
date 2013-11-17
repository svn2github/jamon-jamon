/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import junit.framework.TestCase;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class SequentialListTest extends TestCase {
  public void testOneEmptyList() {
    assertListEquals(new SequentialList<Integer>(make()));
  }

  public void testOneList() {
    assertListEquals(new SequentialList<Integer>(make(1, 2)), 1, 2);
  }

  public void testTwoLists() {
    assertListEquals(new SequentialList<Integer>(make(1, 2), make(3, 4)), 1, 2, 3, 4);
  }

  public void testTwoListsFirstEmpty() {
    assertListEquals(new SequentialList<Integer>(make(), make(1, 2)), 1, 2);
  }

  public void testTwoItersSecondEmpty() {
    assertListEquals(new SequentialList<Integer>(make(1, 2), make()), 1, 2);
  }

  public void testThreeItersSecondEmpty() {
    assertListEquals(new SequentialList<Integer>(make(1, 2), make(), make(3, 4)), 1, 2, 3, 4);
  }

  private static List<Integer> make(Integer... ints) {
    return Arrays.asList(ints);
  }

  private void assertListEquals(List<Integer> expected, int... ints) {
    assertEquals(ints.length, expected.size());
    // test get and iterator
    Iterator<Integer> iterator = expected.iterator();
    for (int i = 0; i < ints.length; i++) {
      assertEquals(ints[i], expected.get(i).intValue());
      assertTrue(iterator.hasNext());
      assertEquals(ints[i], iterator.next().intValue());
    }
    assertFalse(iterator.hasNext());
    try {
      iterator.next();
      fail("next should have thrown an exception");
    }
    catch (NoSuchElementException e) {}
  }
}
