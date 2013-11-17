/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.Fragment;
import test.jamon.EscapedFragment;
import test.jamon.RenderedFragment;
import test.jamon.RepeatedFragmentName;

/**
 * Test Jamon's template fragments. See "Jamon User's Guide", section 8.
 **/

public class FragmentTest extends TestBase {

  public void testExercise() throws Exception {
    new Fragment().render(getWriter(), 1);
    checkOutput("1(2)1");
  }

  public void testMakeRenderer() throws Exception {
    new RenderedFragment().render(getWriter());
    checkOutput("<");
  }

  public void testEscaping() throws Exception {
    new EscapedFragment().render(getWriter());
    checkOutput("<&lt;");
  }

  public void testRepeatedFragmentNameExercise() throws Exception {
    new RepeatedFragmentName().render(getWriter());
    checkOutput("d1:d1,d2:d2,d3:d3 - 7");
  }

  public void testFragmentArgInNamedFragmentImpl() throws Exception {
    expectParserError("FragmentArgInNamedFragmentImpl",
      "Fragment args for fragments not implemented", 3, 3);
  }

  public void testFragmentArgInAnonFragmentImpl() throws Exception {
    expectParserError("FragmentArgInAnonFragmentImpl",
      "Fragment args for fragments not implemented", 3, 3);
  }

}
