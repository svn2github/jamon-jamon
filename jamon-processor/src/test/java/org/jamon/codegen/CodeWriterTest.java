/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class CodeWriterTest extends TestCase {
  private ByteArrayOutputStream bytes;

  private CodeWriter codeWriter;
  private String nl = System.getProperty("line.separator");

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
    checkOutput("(outer1={}, outer2={" + nl + "  mid1=[inner1, inner2]," + nl + "  mid2}, outer3)");
  }

  private void checkOutput(String expected) throws IOException {
    codeWriter.finish();
    assertEquals(expected, new String(bytes.toByteArray(), "US-ASCII"));
  }
}
