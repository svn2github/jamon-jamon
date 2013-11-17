/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

public class DuplicateCallNameTest extends TestBase {
  public void testDefAndMethod() throws Exception {
    expectParserError("LikeNamedDefAndMethod", "multiple defs and/or methods named foo", 2, 1);
  }

  public void testDefAndDef() throws Exception {
    expectParserError("LikeNamedDefAndDef", "multiple defs and/or methods named foo", 2, 1);
  }

  public void testDefAndInheritedMethod() throws Exception {
    expectParserError("LikeNamedDefAndInheritedMethod", "multiple defs and/or methods named foo",
      2, 1);
  }
}
