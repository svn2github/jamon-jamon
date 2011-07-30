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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s): Jay Sachs
 */

package org.jamon.codegen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class CodeWriterTest extends TestCase {
  private ByteArrayOutputStream bytes;

  private CodeWriter codeWriter;

  @Override
  public void setUp() throws Exception {
    bytes = new ByteArrayOutputStream();
    codeWriter = new CodeWriter(bytes, "US-ASCII");
  }

  public void testIndentation() throws Exception {
    codeWriter.println("line1");
    codeWriter.openBlock();
    codeWriter.println("line3");
    codeWriter.openBlock();
    codeWriter.println("line5");
    codeWriter.indent();
    codeWriter.println("line6");
    codeWriter.outdent();
    codeWriter.closeBlock();
    codeWriter.closeBlock("suffix");
    codeWriter.println("line9");
    String nl = System.getProperty("line.separator");
    checkOutput(
      "line1" + nl + "{" + nl + "  line3" + nl + "  {" + nl + "    line5" + nl
      + "      line6" + nl + "  }" + nl + "}suffix" + nl + "line9" + nl);
  }

  public void testFinishIndentCheck() throws IOException {
    codeWriter.openBlock();
    try {
      codeWriter.finish();
      fail("no exception thrown");
    }
    catch (IllegalStateException e) {}
    codeWriter.closeBlock();
    codeWriter.finish();
  }

  public void testFinishListCheck() throws IOException {
    codeWriter.openList();
    try {
      codeWriter.finish();
      fail("no exception thrown");
    }
    catch (IllegalStateException e) {}
    codeWriter.closeList();
    codeWriter.finish();
  }

  public void testClosingUnopenedList() {
    try {
      codeWriter.closeList();
      fail("no exception thrown");
    }
    catch (IllegalStateException e) {}
  }

  public void testPrintArgOutsideOfList() {
    try {
      codeWriter.printListElement("foo");
      fail("no exception thrown");
    }
    catch (IllegalStateException e) {}
  }

  public void testNoArgList() throws IOException {
    codeWriter.openList();
    codeWriter.closeList();
    checkOutput("()");
  }

  public void testOneArgList() throws IOException {
    codeWriter.openList();
    codeWriter.printListElement("foo");
    codeWriter.closeList();
    checkOutput("(foo)");
  }

  public void testTwoArgList() throws IOException {
    codeWriter.openList();
    codeWriter.printListElement("foo");
    codeWriter.printListElement("bar");
    codeWriter.closeList();
    checkOutput("(foo, bar)");
  }

  public void testThreeArgList() throws IOException {
    codeWriter.openList();
    codeWriter.printListElement("foo");
    codeWriter.printListElement("bar");
    codeWriter.printListElement("baz");
    codeWriter.closeList();
    checkOutput("(foo, bar, baz)");
  }

  public void testNestedList() throws Exception {
    codeWriter.openList();
    codeWriter.printListElement("outer1=");
    codeWriter.openList("{", true);
    codeWriter.closeList("}");
    codeWriter.printListElement("outer2=");
    codeWriter.openList("{", true);
    codeWriter.printListElement("mid1=");
    codeWriter.openList("[", false);
    codeWriter.printListElement("inner1");
    codeWriter.printListElement("inner2");
    codeWriter.closeList("]");
    codeWriter.printListElement("mid2");
    codeWriter.closeList("}");
    codeWriter.printListElement("outer3");
    codeWriter.closeList();
    checkOutput("(outer1={}, outer2={\n  mid1=[inner1, inner2],\n  mid2}, outer3)");
  }

  private void checkOutput(String expected) throws IOException {
    codeWriter.finish();
    assertEquals(expected, new String(bytes.toByteArray(), "US-ASCII"));
  }
}
