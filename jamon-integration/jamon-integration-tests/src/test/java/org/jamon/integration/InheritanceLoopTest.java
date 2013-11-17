/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

public class InheritanceLoopTest extends TestBase {
  public void testInheritanceLoop() throws Exception {
    expectParserError(
      "ParentLoop2",
      "cyclic inheritance or replacement involving /test/jamon/broken/ParentLoop1", 1, 1);
  }
}
