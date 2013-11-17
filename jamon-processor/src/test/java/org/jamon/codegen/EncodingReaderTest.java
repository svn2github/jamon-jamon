/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
