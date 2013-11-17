/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.integration;

import org.junit.Test;

import test.jamon.legacy.*;

public class Legacy23Test extends TestBase {
  @Test
  public void testLegacy() throws Exception {
    new CallerOfLegacy().render(getWriter());
    assertEquals("3", getOutput());
  }

  @Test
  public void testLegacyFrag() throws Exception {
    new CallerOfLegacyFrag().render(getWriter());
    assertEquals("Frag says x is 3", getOutput());
  }

  @Test
  public void testChildOfLegacyParent() throws Exception {
    new ChildOfLegacyParent().render(getWriter(), 3, 4);
    assertEquals("Parent 3/Child 3-4", getOutput());
  }

  @Test
  public void testChildOfLegacyParentWithFrag() throws Exception {
    new CallerOfChildOfLegacyParentWithFrag().render(getWriter());
    assertEquals("Parent: [3]\nChild: child", getOutput());
  }

}
