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
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import junit.framework.TestCase;

public class EncodingReaderTest extends TestCase {
  public void testEmptyStream() throws Exception {
    EncodingReader reader = makeReader(new byte[0]);
    assertEquals("", readAll(reader));
    assertEquals(Charset.defaultCharset().name(), reader.getEncoding());
  }

  public void testOneCharStream() throws Exception {
    EncodingReader reader = makeReader(new byte[] { 'x' });
    assertEquals("x", readAll(reader));
    assertEquals(Charset.defaultCharset().name(), reader.getEncoding());
  }

  public void testNoEncodingTag() throws Exception {
    String stuff = "abcdefg12345!@#$%^~";
    EncodingReader reader = new EncodingReader(new ByteArrayInputStream(stuff.getBytes("latin1")));
    assertEquals(stuff, readAll(reader));
    assertEquals(Charset.defaultCharset().name(), reader.getEncoding());
  }

  public void testLatin1() throws Exception {
    doTest("latin1", "abcdefg12345!@#$%^\u00B2\u00EC");
    doTest("ISO8859-1", "abcdefg12345!@#$%^\u00B2\u00EC");
  }

  public void testUtf8() throws Exception {
    doTest("utf-8", "abcdefg12345!@#$%^\u00B2\u00EC\u3092");
  }

  public void testUtf16() throws Exception {
    // use utf-16be not utf-16 since jdk1.3 is broken
    // and insists on a byte-order mark for "vanilla" utf-16
    doTest("UTF-16BE", "abcdefg12345!@#$%^\u00B2\u00EC\u3092");
  }

  private void doTest(final String encoding, final String stuff) throws Exception {
    StringWriter writer = new StringWriter();
    writer.write("<%encoding \t ");
    writer.write(encoding);
    writer.write("  >    \n\t  \n");
    writer.write(stuff);
    EncodingReader reader = makeReader(writer.toString().getBytes(encoding));
    assertEquals(stuff, readAll(reader));
    assertEquals(encoding, reader.getEncoding());
  }

  private EncodingReader makeReader(byte[] bytes) throws IOException {
    return new EncodingReader(new ByteArrayInputStream(bytes));
  }

  private String readAll(Reader reader) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(reader);
    StringBuilder buf = new StringBuilder();
    String s = null;
    while ((s = bufferedReader.readLine()) != null) {
      buf.append(s);
    }
    return buf.toString();
  }
}
