/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.RenderTo;

/**
 * Test Jamon's renderTo() method.
 **/

public class RenderToTest extends TestBase {

  public void testExercise() throws Exception {
    new RenderTo().render(getWriter());
    checkOutput("x1AaXbB2\n");
  }

}
