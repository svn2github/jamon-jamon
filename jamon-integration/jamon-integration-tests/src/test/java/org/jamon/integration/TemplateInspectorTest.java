/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import org.jamon.compiler.TemplateInspector;

import java.util.List;

public class TemplateInspectorTest extends TestBase {
  public void testArgumentReflection() throws Exception {
    TemplateInspector inspector = new TemplateInspector("/test/jamon/Grandchild");
    List<String> args = inspector.getRequiredArgumentNames();
    assertEquals(5, args.size());
    assertEquals("i", args.get(0));
    assertEquals("j", args.get(1));
    assertEquals("a", args.get(2));
    assertEquals("b", args.get(3));
    assertEquals("x", args.get(4));
    assertEquals(Integer.TYPE, inspector.getArgumentType("i"));
    assertEquals(Integer.class, inspector.getArgumentType("j"));
    assertEquals(String.class, inspector.getArgumentType("a"));
    assertEquals(Boolean.TYPE, inspector.getArgumentType("b"));
    assertEquals(Boolean.class, inspector.getArgumentType("x"));

    args = inspector.getOptionalArgumentNames();
    assertEquals(10, args.size());
    for (int i = 1; i <= 10; ++i) {
      String name = "opt" + i;
      assertTrue(args.contains(name));
      assertEquals(String.class, inspector.getArgumentType(name));
    }
  }
}
