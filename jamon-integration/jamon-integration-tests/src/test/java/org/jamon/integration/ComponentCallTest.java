/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

public class ComponentCallTest extends TestBase {
  public void testCallToNonexistantTemplate() throws Exception {
    expectParserError(
      "CallToNonexistantTemplate", "Unable to find template or class for /foo/bar", 1, 1);
  }
}
