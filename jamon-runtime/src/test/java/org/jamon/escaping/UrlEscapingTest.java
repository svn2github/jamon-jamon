/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.escaping;

import java.io.Writer;
import java.io.StringWriter;

import junit.framework.TestCase;

public class UrlEscapingTest extends TestCase {
  public void testEncoding() throws Exception {
    Writer writer = new StringWriter();
    Escaping.URL.write("a+b c%d", writer);
    assertEquals((Object) "a%2Bb+c%25d", writer.toString());
  }
}
