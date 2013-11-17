/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

/**
 * Test Jamon's escaping directive.
 **/

public class EscapeDirectiveTest extends TestBase {
  public void testExercise() throws Exception {
    new test.jamon.EscapeDirective().render(getWriter());
    checkOutput("+%25x");
  }

  public void testBadDirective() throws Exception {
    expectParserError("BadEscaping", "Unknown escaping directive 'Q'", 1, 6);
  }

  public void testBadDefault() throws Exception {
    expectParserError("BadDefaultEscaping", "Unknown escaping directive 'Z'", 1, 1);
  }

}
