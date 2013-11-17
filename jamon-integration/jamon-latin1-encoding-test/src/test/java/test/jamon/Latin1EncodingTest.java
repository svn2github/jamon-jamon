/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package test.jamon;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;


public class Latin1EncodingTest {
  @Test
  public void testLatin1()
  throws Exception
  {
    StringWriter stringWriter = new StringWriter();
    new test.jamon.Latin1().render(stringWriter);
    assertEquals("Onc\u00e9\u00f2\u00e1\n\u00e9\u00c1\u00f3\u00e9\n" +
        "Onc\u00e9\u00f2\u00e1 \u00e9\u00c1\u00f3\u00e9\n" +
        "Onc\u00e9\u00f2\u00e1", stringWriter.toString());
  }

  @Test
  public void testParentDefaultEncoding()
  throws Exception
  {
    StringWriter stringWriter = new StringWriter();
    new test.jamon.Latin1Child().render(stringWriter);
    assertEquals(
      "Parent\nOnc\u00e9\u00f2\u00e1\n\u00e9\u00c1\u00f3\u00e9\n" +
        "Onc\u00e9\u00f2\u00e1 \u00e9\u00c1\u00f3\u00e9\n" +
        "Onc\u00e9\u00f2\u00e1",
      stringWriter.toString());
  }
}
