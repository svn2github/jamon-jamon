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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

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
