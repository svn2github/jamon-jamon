/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.subdir.RelativePath;

public class RelativePathTest extends TestBase {
  public void testRelativePath() throws Exception {
    new RelativePath().render(getWriter());
    checkOutput("simple");
  }

  public void testToManyDotDots() throws Exception {
    expectParserError("TooManyDotDots", "Cannot reference templates above the root", 1, 13);
  }
}
