/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

/**
 * Test Jamon's escape mechanisms.
 **/

public class EscapeTest extends TestBase {
  public void testExercise() throws Exception {
    new test.jamon.Escapes().render(getWriter());
    checkOutput(
      "This is how to escape a newline in Java: \\n\n"
      + "This is how to escape a newline in Java: \\\"\n"
      + "And this mess \\\" \\n \\\\ is on one line.\\");
  }

  public void testEscapeProperty() throws Exception {
    new test.jamon.escapeProperties.PropertyEscaped().render(getWriter());
    checkOutput("a+b");
  }
}
