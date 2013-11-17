/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

/**
 * Test Jamon's encoding mechanisms.
 **/

public class EncodingsTest extends TestBase {
  public void testUtf8() throws Exception {
    new test.jamon.Utf8().render(getWriter());
    checkOutput(
      "Onc\u00e9\u00f2\u00e1\n\u00e9\u00c1\u00f3\u00e9\n"
      + "Onc\u00e9\u00f2\u00e1 \u00e9\u00c1\u00f3\u00e9\nOnc\u00e9\u00f2\u00e1");
  }

  public void testMalformedEncoding1() throws Exception {
    expectParserError("BadEncoding", "Malformed encoding tag; expected '>'", 1, 16);
  }

  public void testMalformedEncoding2() throws Exception {
    expectParserError("BadEncoding2", "EOF before encoding tag finished", 1, 12);
  }

  public void testMalformedEncoding3() throws Exception {
    try {
      generateSource("test/jamon/broken/BadEncoding3");
      fail();
    }
    catch (java.io.UnsupportedEncodingException e) {
      // expected
    }
  }

}
