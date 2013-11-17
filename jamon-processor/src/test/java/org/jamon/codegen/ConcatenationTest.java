/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class ConcatenationTest {
  @Test
  public void twoEmpyCollections() {
    assertCollectionsEqual(new Concatenation<Integer>(make(), make()), Collections.<Integer>emptyList());
  }

  @Test
  public void twoCollections() {
    assertCollectionsEqual(new Concatenation<Integer>(make(1, 2), make(3, 4)), Arrays.asList(1, 2, 3, 4));
  }

  @Test
  public void threeCollections() {
    assertCollectionsEqual(
      new Concatenation<Integer>(make(1, 2), make(5, 6), make(3, 4)),
      Arrays.asList(1, 2, 5, 6, 3, 4));
  }

  private static List<Integer> make(Integer... ints) {
    return Arrays.asList(ints);
  }

  private static void assertCollectionsEqual(Collection<Integer> actual, List<Integer> expected) {
    assertEquals(expected, new ArrayList<Integer>(actual));
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(ConcatenationTest.class);
  }
}
