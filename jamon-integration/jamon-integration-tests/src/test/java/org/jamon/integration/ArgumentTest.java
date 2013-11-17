/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.Arguments;
import test.jamon.OptionalArguments;
import test.jamon.OptionalDefArguments;

/**
 * Test Jamon's parameterized templates. See "Jamon User's Guide", section 5.
 **/

public class ArgumentTest extends TestBase {

  public void testExercise() throws Exception {
    new Arguments().render(getWriter(), INT, BOOLEAN, STRING);
    checkOutput("" + INT + BOOLEAN + STRING);
  }

  public void testOptional1() throws Exception {
    new OptionalArguments().setI(INT).render(getWriter(), BOOLEAN, STRING);
    checkOutput("" + INT + BOOLEAN + STRING);
  }

  public void testOptional2() throws Exception {
    new OptionalArguments().render(getWriter(), BOOLEAN, STRING);
    checkOutput("" + 0 + BOOLEAN + STRING);
  }

  public void testOptionalDef() throws Exception {
    new OptionalDefArguments().render(getWriter());
    checkOutput("" + 0 + BOOLEAN + "s" + "\n" + "1" + BOOLEAN + "s");
  }

  private static final int INT = 3;
  private static final boolean BOOLEAN = true;
  private static final String STRING = "foobar";

}
