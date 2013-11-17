/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.ConcreteMethod;

public class AbstractMethodTest extends TestBase {
  public void testIntrospection() throws Exception {
    getRecompilingTemplateManager().constructProxy("/test/jamon/external/ConcreteMethod");
  }

  public void testAbstractMethod() throws Exception {
    new ConcreteMethod().render(getWriter());
    checkOutput("3 some content");
  }

  public void testNoOverride() throws Exception {
    expectParserError(
      "MissingOverride", "The abstract method(s) foo have no concrete implementation", 1, 1);
  }

  public void testAbstractMethodInConcreteTemplate() throws Exception {
    expectParserError(
      "AbstractMethodInConcrete", "Non-abstract templates cannot have abstract methods", 1, 1);
  }
}
