/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.Alias;
import test.jamon.aliasProperties.PropertiesAlias;

/**
 * Test Jamon's aliases.
 **/

public class AliasTest extends TestBase {
  public void testAliasesFromTemplate() throws Exception {
    new Alias().render(getWriter());
    checkOutput("simple simple simple");
  }

  public void testAliasesFromProperties() throws Exception {
    new PropertiesAlias().render(getWriter());
    checkOutput("simple simple");
  }
}
