/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import java.math.BigDecimal;

import test.jamon.TestTemplate;

public class FirstTest extends TestBase {

  public void testExercise() throws Exception {
    new TestTemplate().setX(57).render(getWriter(), new BigDecimal("34.5324"));

    checkOutputContains(
      "An external template with a parameterized fragment parameter (farg)\n\n  \n"
      + "  i is 3 and s is yes.\n\n  \n  i is 7 and s is no");
  }

}
