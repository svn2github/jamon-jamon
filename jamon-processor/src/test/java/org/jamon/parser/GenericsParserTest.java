/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import static org.junit.Assert.*;

import org.jamon.node.GenericsParamNode;
import org.jamon.node.GenericsBoundNode;
import org.jamon.node.GenericsNode;
import org.jamon.node.TopNode;
import org.junit.Test;

public class GenericsParserTest extends AbstractParserTest {
  private final static String UNEXPECTED_CLOSE = "Unexpected tag close </%generic>";

  @Test
  public void testNoParams() throws Exception {
    assertErrorPair(
      "<%generic>\n</%generic>",
      2, 1, GenericsParser.TYPE_PARAMETER_EXPECTED_ERROR,
      2, 1, UNEXPECTED_CLOSE);
  }

  @Test
  public void testSimpleParam() throws Exception {
    assertEquals(
      topGenericsNode(new GenericsParamNode(location(2, 1), "T")),
      parse("<%generic>\nT</%generic>"));
  }

  @Test
  public void testSimpleParams() throws Exception {
    assertEquals(
      topGenericsNode(
        new GenericsParamNode(location(2, 1), "T"),
        new GenericsParamNode(location(2, 4), "S")),
      parse("<%generic>\nT, S</%generic>"));
  }

  @Test
  public void testBoundedParam() throws Exception {
    assertEquals(
      topGenericsNode(
        new GenericsParamNode(location(2, 1), "T")
          .addBound(new GenericsBoundNode(location(3, 1), "Foo"))),
      parse("<%generic>\nT extends\nFoo</%generic>"));
  }

  @Test
  public void testMultiplyBoundedParam() throws Exception {
    assertEquals(
      topGenericsNode(
        new GenericsParamNode(location(2, 1), "T")
          .addBound(new GenericsBoundNode(location(3, 1), "Foo"))
          .addBound(new GenericsBoundNode(location(3, 5), "bar.Baz"))),
      parse("<%generic>\nT extends\nFoo&bar.Baz</%generic>"));
  }

  @Test
  public void testBoundedParams() throws Exception {
    assertEquals(
      topGenericsNode(
        new GenericsParamNode(location(2, 1), "T")
          .addBound(new GenericsBoundNode(location(3, 1), "Foo")),
        new GenericsParamNode(location(4, 1), "S")
          .addBound(new GenericsBoundNode(location(5, 1), "Bar"))
          .addBound(new GenericsBoundNode(location(5, 7), "Baz"))),
      parse("<%generic>\nT extends\nFoo,\nS extends\nBar & Baz</%generic>"));
  }

  @Test
  public void testBadParamName() throws Exception {
    assertErrorPair("<%generic>\na.b\n</%generic>", 2, 2,
      GenericsParser.EXPECTING_EXTENDS_OR_GENERIC_ERROR, 3, 1, UNEXPECTED_CLOSE);
  }

  @Test
  public void testExpectingExtends() throws Exception {
    assertErrorPair("<%generic>\na foo\n</%generic>", 2, 3,
      GenericsParser.EXPECTING_EXTENDS_OR_GENERIC_ERROR, 3, 1, UNEXPECTED_CLOSE);
  }

  @Test
  public void testBadBounds() throws Exception {
    assertErrorPair("<%generic>a extends\n*</%generic>", 2, 1,
      AbstractParser.BAD_JAVA_TYPE_SPECIFIER, 2, 2, UNEXPECTED_CLOSE);
  }

  private static TopNode topGenericsNode(GenericsParamNode... params) {
    TopNode topNode = topNode();
    GenericsNode genericsNode = new GenericsNode(START_LOC);
    topNode.addSubNode(genericsNode);
    for (GenericsParamNode param: params) {
      genericsNode.addParam(param);
    }
    return topNode;
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(GenericsParserTest.class);
  }
}
