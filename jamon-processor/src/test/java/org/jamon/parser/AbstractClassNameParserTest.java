/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractNode;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class AbstractClassNameParserTest extends AbstractParserTest {
  @Test
  public void testParseSimple() throws Exception {
    assertEquals("foo", parseTypeName("foo "));
  }

  @Test
  public void testParseCompound() throws Exception {
    assertEquals("foo.bar", parseTypeName("foo. bar"));
  }

  @Test
  public void testParameterized() throws Exception {
    assertEquals("foo<Integer>", parseTypeName("foo < Integer >"));
  }

  @Test
  public void testParameterizedWithWildcard() throws Exception {
    assertEquals("foo<?>", parseTypeName("foo < ? >"));
  }

  @Test
  public void testParameterizedWithBoundWildcard() throws Exception {
    assertEquals("foo<? extends a.b>", parseTypeName("foo < ? extends a . b >"));
    assertEquals("foo<? super a.b<c>>", parseTypeName("foo < ? super a.b< c > >"));
  }

  @Test
  public void testMultipleParameters() throws Exception {
    assertEquals("foo.bar<T,S>", parseTypeName("foo . bar < T , S >"));
  }

  @Test
  public void testComplexParameters() throws Exception {
    String complexType = "foo<a,b>.bar<? extends baz.bar<T[]>,a.b<x>,? super c.d<S>>";
    assertEquals(complexType, parseTypeName(complexType));
  }

  @Test
  public void testParameterizingDottedName() throws Exception {
    assertError("foo<bar.baz extends flap>", 1, 1, AbstractParser.BAD_JAVA_TYPE_SPECIFIER);
  }

  /**
   * Provided for use with the AssertError method
   */
  @Override
  protected AbstractNode parse(String content) throws IOException {
    parseTypeName(content);
    return null;
  }

  protected abstract ClassNameParser makeParser(
    Location location, PositionalPushbackReader reader, ParserErrorsImpl errors)
  throws IOException, ParserErrorImpl;

  protected String parseTypeName(String content) throws IOException {
    ParserErrorsImpl errors = new ParserErrorsImpl();
    String result = null;
    try {
      ClassNameParser parser = makeParser(START_LOC, makeReader(content), errors);
      result = parser.getType();
    }
    catch (ParserErrorImpl e) {
      errors.addError(e);
    }
    if (errors.hasErrors()) {
      throw errors;
    }
    return result;
  }
}
