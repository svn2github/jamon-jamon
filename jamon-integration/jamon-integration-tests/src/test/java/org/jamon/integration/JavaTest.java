/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.JavaEscape;

/**
 * Test Jamon's java escapes. See "Jamon User's Guide", section 2.
 **/

public class JavaTest extends TestBase {

  public void testExercise() throws Exception {
    new JavaEscape().render(getWriter());
    checkOutput("0\n1\n2\ntrue");
  }

}
