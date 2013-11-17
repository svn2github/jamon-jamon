/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import org.junit.Test;

public class ErrorRecoveryTest extends AbstractParserTest {
  @Test
  public void testCloseTagRecovery() throws Exception {
    assertErrorPair(
      "</%foo></%bar>",
      1, 1, "Unexpected tag close </%foo>",
      1, 8, "Unexpected tag close </%bar>");
    assertErrorPair(
      "<%def bob></%foo></%def>",
      1, 11, "Unexpected tag close </%foo>",
      1, 18, "Unexpected tag close </%def>");
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(ErrorRecoveryTest.class);
  }
}
