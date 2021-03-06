/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

public class CodeWriter {
  public CodeWriter(OutputStream stream, String encoding) {
    try {
      writer = new PrintWriter(new OutputStreamWriter(stream, encoding));
    }
    catch (UnsupportedEncodingException e) {
      // us-ascii is guaranteed to be available
      throw new RuntimeException(e);
    }
  }

  private final PrintWriter writer;

  private int indentation = 0;

  private LinkedList<Boolean> argAlreadyPrintedStack = new LinkedList<Boolean>();

  private LinkedList<Boolean> itemPerLineStack = new LinkedList<Boolean>();

  private int nextFragmentImplCounter = 0;

  private boolean beginingOfLine = true;

  private static final int BASIC_OFFSET = 2;

  private static final String SPACES = "                                        "; // 40 spaces

  public int nextFragmentImplCounter() {
    return nextFragmentImplCounter++;
  }

  public void printLocation(org.jamon.api.Location location) {
    // In some cases, (such as children of class-only templates), we have
    // no location.
    if (location != null) {
      println("// " + location.getLine() + ", " + location.getColumn());
    }
  }

  public void println() {
    println("");
  }

  public void println(Object obj) {
    maybeIndent();
    writer.println(obj);
    beginingOfLine = true;
  }

  public void print(Object obj) {
    maybeIndent();
    writer.print(obj);
    beginingOfLine = false;
  }

  public void openBlock() {
    println("{");
    indent();
  }

  public void closeBlock(String extra) {
    outdent();
    println("}" + extra);
  }

  public void closeBlock() {
    closeBlock("");
  }

  public void indent() {
    indentation += BASIC_OFFSET;
  }

  public void outdent() {
    indentation -= BASIC_OFFSET;
    if (indentation < 0) {
      throw new IllegalStateException("Attempting to outdent past 0");
    }
  }

  public void openList() {
    openList("(", false);
  }

  public void openList(String openToken, boolean itemPerLine) {
    argAlreadyPrintedStack.addLast(Boolean.FALSE);
    itemPerLineStack.addLast(Boolean.valueOf(itemPerLine));
    print(openToken);
    if (itemPerLine) {
      indent();
    }
  }

  public void closeList() {
    closeList(")");
  }

  public void closeList(String closeToken) {
    if (argAlreadyPrintedStack.isEmpty()) {
      throw new IllegalStateException("Attempt to close unopened list");
    }
    argAlreadyPrintedStack.removeLast();
    if (itemPerLineStack.removeLast()) {
      outdent();
    }
    print(closeToken);
  }

  public void printListElement(String listElement) {
    if (argAlreadyPrintedStack.isEmpty()) {
      throw new IllegalStateException("Attempt to print arg outside of list");
    }
    if (argAlreadyPrintedStack.getLast()) {
      if (itemPerLineStack.getLast()) {
        println(",");
      }
      else {
        print(", ");
      }
    }
    else {
      argAlreadyPrintedStack.removeLast();
      argAlreadyPrintedStack.addLast(Boolean.TRUE);
      if (itemPerLineStack.getLast()) {
        println();
      }
    }
    print(listElement);
  }

  public void finish() throws IOException {
    if (indentation != 0) {
      throw new IllegalStateException("indentation is " + indentation + " at end of file");
    }
    if (!argAlreadyPrintedStack.isEmpty()) {
      throw new IllegalStateException("in a list at end of file");
    }
    try {
      if (writer.checkError()) {
        throw new IOException("Exception writing to stream");
      }
    }
    finally {
      writer.close();
    }
  }

  private void maybeIndent() {
    if (beginingOfLine) {
      writer.print(SPACES.substring(0, Math.min(indentation, SPACES.length())));
    }
  }
}
