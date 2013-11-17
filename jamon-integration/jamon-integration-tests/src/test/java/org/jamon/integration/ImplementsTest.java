/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.Implements;

/**
 * Test Jamon's implements.
 **/

public class ImplementsTest extends TestBase {
  private static final int INT = 3;
  private static final String STRING = "foobar";

  public void testImplements() throws Exception {
    Implements i = new Implements();
    i.setX(INT);
    SomeInterface x = i;
    x.render(getWriter(), STRING);
    checkOutput("" + INT + STRING);

    // FIXME: want to do

    // SomeInterface x = new Implements();
    // x.writeTo(getWriter()).setX(INT).render(STRING);
  }
}
