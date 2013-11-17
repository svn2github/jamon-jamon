/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractNode;
import org.jamon.node.ArgsNode;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArgsParserTest extends AbstractParserTest {
  private static String ARGS_START = ">";
  private static String ARGS_END = "</%args>";

  @Override
  protected AbstractNode parse(String text) throws IOException {
    final PositionalPushbackReader reader = makeReader(text);
    ParserErrorsImpl errors = new ParserErrorsImpl();
    try {
      ArgsNode result = new ArgsParser(reader, errors, START_LOC).getArgsNode();
      if (errors.hasErrors()) {
        throw errors;
      }
      else {
        return result;
      }
    }
    catch (ParserErrorImpl e) {
      errors.addError(e);
      throw errors;
    }
  }

  private static ArgsNode argsNode() {
    return new ArgsNode(START_LOC);
  }

  @Test
  public void testNoArgs() throws Exception {
    assertEquals(argsNode(), parse(ARGS_START + "   " + ARGS_END));
  }

  @Test
  public void testRequiredArgs() throws Exception {
    assertEquals(
      argsNode()
        .addArg(argNode(location(2, 1), "int", location(2, 5), "i"))
        .addArg(argNode(location(3, 1), "String", location(3, 8), "s")),
      parse(ARGS_START + "\nint i;\nString s;\n" + ARGS_END));
  }

  @Test
  public void testOldStyleOptionalArgs() throws Exception {
    assertEquals(
      argsNode()
        .addArg(optArgNode(location(2, 1), "int", location(2, 5), "i", location(2, 10), "3"))
        .addArg(optArgNode(location(3, 1), "String", location(3, 8), "s", location(3, 13), "\";\"")),
      parse(ARGS_START + "\nint i => 3;\nString s => \";\";\n" + ARGS_END));
  }

  @Test
  public void testNewStyleOptionalArgs() throws Exception {
    assertEquals(
      argsNode()
        .addArg(optArgNode(location(2, 1), "int", location(2, 5), "i", location(2, 9), "3"))
        .addArg(optArgNode(location(3, 1), "String",location(3, 8), "s", location(3, 12), "\";\"")),
      parse(ARGS_START + "\nint i = 3;\nString s = \";\";\n" + ARGS_END));

  }

  @Test
  public void testFullyQualifiedClassNames() throws Exception {
    assertEquals(
      argsNode().addArg(argNode(location(2, 1), "foo.bar", location(2, 9), "x")),
      parse(ARGS_START + "\nfoo.bar x;\n" + ARGS_END));
  }

  @Test
  public void testArrays() throws Exception {
    assertEquals(
      argsNode().addArg(argNode(location(2, 1), "int[]", location(2, 7), "x")),
      parse(ARGS_START + "\nint[] x;\n" + ARGS_END));
  }

  @Test
  public void testWhitespace() throws Exception {
    assertEquals(
      argsNode()
        .addArg(argNode(location(2, 1), "a.b[]", location(3, 1), "foo"))
        .addArg(optArgNode(location(4, 1), "a.b", location(5, 1), "x", location(6, 1), "3 ")),
      parse(ARGS_START + "\na . b [ ]\nfoo ;\na . b\nx =>\n3 ;" + ARGS_END));
  }

  @Test
  public void testBadArray() throws Exception {
    assertError(
      ARGS_START + "\nint[ x;" + ARGS_END, 2, 6, AbstractParser.INCOMPLETE_ARRAY_SPECIFIER_ERROR);
  }

  @Test
  public void testMissingName() throws Exception {
    assertError(ARGS_START + "\nf;" + ARGS_END, 2, 2, AbstractParser.NOT_AN_IDENTIFIER_ERROR);
  }

  @Test
  public void testMissingSemiAfterName() throws Exception {
    assertError(
      ARGS_START + "\nf a" + ARGS_END, 2, 4, OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
  }

  @Test
  public void testMissingSemiAfterValue() throws Exception {
    assertError(ARGS_START + "\nf a => c" + ARGS_END, 2, 8, ArgsParser.EOF_LOOKING_FOR_SEMI);
  }

  @Test
  public void testBadArgCloseTag() throws Exception {
    assertError(ARGS_START + "\n<foo", 2, 1, AbstractParser.BAD_ARGS_CLOSE_TAG);
  }

  @Test
  public void testEofAfterArgStart() throws Exception {
    assertErrorTripple(
      ARGS_START,
      1, ARGS_START.length() + 1, AbstractParser.BAD_JAVA_TYPE_SPECIFIER,
      1, ARGS_START.length() + 1, AbstractParser.NOT_AN_IDENTIFIER_ERROR,
      1, ARGS_START.length() + 1, OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);

  }

  @Test
  public void testEofLookingForName() throws Exception {
    assertErrorPair(
      ARGS_START + "\na",
      2, 2, AbstractParser.NOT_AN_IDENTIFIER_ERROR,
      2, 2, OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
  }

  @Test
  public void testEofLookingForPostNameSemi() throws Exception {
    assertError(ARGS_START + "\na b", 2, 4, OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
  }

  @Test
  public void testEofLookingForValue() throws Exception {
    assertError(ARGS_START + "\na b =>\n", 3, 1, ArgsParser.EOF_LOOKING_FOR_SEMI);
  }

  @Test
  public void testEofLookingForPostValueSemi() throws Exception {
    assertError(ARGS_START + "\na b =>\nc", 3, 1, ArgsParser.EOF_LOOKING_FOR_SEMI);
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(ArgsParserTest.class);
  }
}
