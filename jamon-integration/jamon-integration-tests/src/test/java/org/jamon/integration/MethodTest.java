/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.Method;
import test.jamon.MethodChild;
import test.jamon.MethodOverride;

public class MethodTest extends TestBase {
  public void testSimpleMethod() throws Exception {
    new Method().render(getWriter());
    checkOutput("{foo: req1, po1} {foo: req1, po2} {bar: passed 1}");
  }

  public void testInheritedMethod() throws Exception {
    new MethodChild().render(getWriter());
    final String methodsOutput = "{foo: req1, po1} {foo: req1, po2} {bar: passed 1}";
    checkOutput("{ parent: " + methodsOutput + " }{ child: " + methodsOutput + " }");
  }

  public void testOverriddenMethod() throws Exception {
    new MethodOverride().render(getWriter());
    final String methodsOutput =
      "{fooOverride: req1, co3} {fooOverride: req1, po2} {barOverride: passed 2}";
    checkOutput("{ parent: " + methodsOutput + " }{ child: " + methodsOutput + " }");
  }

  public void testOverrideNonexistentMethod() throws Exception {
    expectParserError(
      "OverrideNonexistentMethod", "There is no such method noSuchMethod to override", 2, 1);
  }
}
