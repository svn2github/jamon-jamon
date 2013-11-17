/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.UnnamedParams;

/**
 * Test unnamed parameters
 **/

public class UnnamedParamsTest extends TestBase {
  public void testDefs() throws Exception {
    new UnnamedParams().render(getWriter());
    checkOutput("3 one 3 one two 3 one 3 one two 3 f 3 g");
  }

  public void testIncorrectNumberOfParams() throws Exception {
    expectParserError(
      "BadUnnamedParamCount", "Call provides 1 arguments when 2 are expected", 8, 5);
  }
}
