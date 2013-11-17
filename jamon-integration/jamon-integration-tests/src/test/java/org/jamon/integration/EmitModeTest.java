/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import org.jamon.compiler.TemplateCompilationException;

import test.jamon.NullStandardEmits;
import test.jamon.PrimativeWrapperStandardEmits;
import test.jamon.limitedEmit.NullLimitedEmits;
import test.jamon.limitedEmit.PrimativeWrapperLimitedEmits;

public class EmitModeTest extends TestBase {
  public void testLimited() throws Exception {
    try {
      getRecompilingTemplateManager().constructProxy("test/jamon/broken/limitedEmit/BadEmit1");
      fail();
    }
    catch (TemplateCompilationException e) {
      // ok
    }
  }

  public void testStrict() throws Exception {
    try {
      getRecompilingTemplateManager().constructProxy("test/jamon/broken/strictEmit/BadEmit2");
      fail();
    }
    catch (TemplateCompilationException e) {
      // ok
    }
  }

  public void testNullStandardEmits() throws Exception {
    checkOutput(new NullStandardEmits().makeRenderer(),
      "bool \nb \nc \ns \ni \nl \nf \nd \nstring ");
  }

  public void testNullLimittedEmits() throws Exception {
    checkOutput(
      new NullLimitedEmits().makeRenderer(),
      "bool \nb \nc \ns \ni \nl \nf \nd \nstring ");
  }

  public void testPrimatveWrapperStandardEmits() throws Exception {
    checkOutput(
      new PrimativeWrapperStandardEmits().makeRenderer(),
      "bool true\nb 3\nc c\ns 3\ni 3\nl 3\nf 3.0\nd 3.0\nstring test");
  }

  public void testPrimatveWrapperLimitedEmits() throws Exception {
    checkOutput(
      new PrimativeWrapperLimitedEmits().makeRenderer(),
      "bool true\nb 3\nc c\ns 3\ni 3\nl 3\nf 3.0\nd 3.0\nstring test");
  }
}
