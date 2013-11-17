/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.MultiFarg;
import test.jamon.MultiFarg2;

public class MultiFargTest extends TestBase {
  public void testSimple() throws Exception {
    new MultiFarg().render(getWriter(), 1);
    checkOutput("1(2)1(8)1");
  }

  public void testComplex() throws Exception {
    new MultiFarg2().render(getWriter(), 2);
    checkOutput("0@2!/4/$S$/5/$T$");
  }
}
