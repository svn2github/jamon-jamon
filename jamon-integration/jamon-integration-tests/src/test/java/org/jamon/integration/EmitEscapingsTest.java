/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.EmitEscaping;

public class EmitEscapingsTest extends TestBase {
  public void testIt() throws Exception {
    new EmitEscaping().render(getWriter());
    checkOutput(
      "default: &lt;'\n"
      + "none: <'\n"
      + "html: &lt;'\n"
      + "strictHtml: &lt;&#39;\n"
      + "url: %3C%27\n"
      + "xml: &lt;&apos;\n");
  }
}
