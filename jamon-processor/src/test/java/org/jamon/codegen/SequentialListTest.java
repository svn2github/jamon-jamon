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
