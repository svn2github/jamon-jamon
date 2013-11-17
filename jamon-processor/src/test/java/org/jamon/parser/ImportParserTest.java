/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.parser;

import static org.junit.Assert.*;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractImportNode;
import org.jamon.node.ImportNode;
import org.jamon.node.StaticImportNode;
import org.junit.Test;

public class ImportParserTest extends AbstractParserTest {
  @Test
  public void testParseSimpleImport() throws Exception {
    assertEquals(new ImportNode(START_LOC, "foo"), parseImport("foo "));
  }

  @Test
  public void testParseCompoundImport() throws Exception {
    assertEquals(new ImportNode(START_LOC, "foo.bar"), parseImport("foo . bar "));
  }

  @Test
  public void testParseStarImport() throws Exception {
    assertEquals(new ImportNode(START_LOC, "foo.bar.*"), parseImport("foo.bar . *"));
  }

  @Test
  public void testStaticImport() throws Exception {
    assertEquals(new StaticImportNode(START_LOC, "foo.bar"), parseImport("static foo.bar"));
  }

  @Test
  public void testBadStaticImport() throws Exception {
    try {
      parseImport("static.foo.bar");
      fail("exception expected");
    }
    catch (ParserErrorImpl e) {
      assertEquals(
        new ParserErrorImpl(START_LOC, ImportParser.MISSING_WHITESPACE_AFTER_STATIC_DECLARATION),
        e);
    }
  }

  private AbstractImportNode parseImport(String p_content) throws Exception {
    ParserErrorsImpl errors = new ParserErrorsImpl();
    AbstractImportNode node =
      new ImportParser(START_LOC, makeReader(p_content), errors).parse().getNode();
    if (errors.hasErrors()) {
      throw errors;
    }
    return node;
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(ImportParserTest.class);
  }
}
