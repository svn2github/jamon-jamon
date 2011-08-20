/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

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
