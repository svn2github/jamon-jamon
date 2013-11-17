/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.ParametrizedFragment;

/**
 * Test Jamon's parametrized template fragments. See "Jamon User's Guide", section 9.
 **/

public class ParametrizedFragmentTest extends TestBase {

  public void testExercise() throws Exception {
    new ParametrizedFragment().render(getWriter(), new int[] { -2, 0, 15 });
    checkOutput("-0+");
  }

}
