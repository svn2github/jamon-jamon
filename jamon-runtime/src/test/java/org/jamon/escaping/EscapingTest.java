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
 * The Initial Developer of the Original Code is Luis O'Shea.  Portions
 * created by Luis O'Shea are Copyright (C) 2003 Luis O'Shea.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

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
