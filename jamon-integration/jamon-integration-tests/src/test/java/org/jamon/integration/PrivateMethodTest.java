/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.PrivateMethods;

/**
 * Test Jamon's private methods. See "Jamon User's Guide", section 6.
 **/

public class PrivateMethodTest extends TestBase {

  public void testExercise() throws Exception {
    new PrivateMethods().render(getWriter());
    checkOutput("7=1111111");
  }

}
