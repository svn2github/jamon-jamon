/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.integration;

import test.jamon.Import;

public class ImportTest extends TestBase {
  public void testExercise() throws Exception {
    new Import().render(getWriter());
    checkOutput("1");
  }
}
