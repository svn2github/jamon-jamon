/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.escaping;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

public class EscapingTest extends TestCase {
  public void testNoEscaping() throws IOException {
    checkEscaping(Escaping.NONE, "<& &gt; &>!\"'");
  }

  public void testHtmlEscaping() throws IOException {
    checkEscaping(Escaping.HTML, "&lt;&amp; &amp;gt; &amp;&gt;!\"'");
  }

  public void testStrictHtmlEscaping() throws IOException {
    checkEscaping(Escaping.STRICT_HTML, "&lt;&amp; &amp;gt; &amp;&gt;!&quot;&#39;");
  }

  public void testUrlEscaping() throws IOException {
    checkEscaping(Escaping.URL, "%3C%26+%26gt%3B+%26%3E%21%22%27");
  }

  public void testXmlEscaping() throws IOException {
    checkEscaping(Escaping.XML, "&lt;&amp; &amp;gt; &amp;&gt;!&quot;&apos;");
  }

  public void testJavascriptEscaping() throws IOException {
    checkEscaping(Escaping.JAVASCRIPT, "<& &gt; &>!\\\"\\'");
  }

  private void checkEscaping(Escaping escaping, String expected) throws IOException {
    check(escaping, "", "");
    check(escaping, "hello", "hello");
    check(escaping, "<& &gt; &>!\"'", expected);
  }

  private void check(Escaping escaping, String text, String expected) throws IOException {
    Writer writer = new StringWriter();
    escaping.write(text, writer);
    assertEquals(expected, writer.toString());
  }
}
