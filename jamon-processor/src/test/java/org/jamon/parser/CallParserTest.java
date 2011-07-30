package org.jamon.parser;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.AbstractCallNode;
import org.jamon.node.AbstractNode;
import org.jamon.node.ChildCallNode;
import org.jamon.node.FragmentCallNode;
import org.jamon.node.GenericCallParam;
import org.jamon.node.MultiFragmentCallNode;
import org.jamon.node.NamedFragmentNode;
import org.jamon.node.NamedParamNode;
import org.jamon.node.NamedParamsNode;
import org.jamon.node.NoParamsNode;
import org.jamon.node.ParamNameNode;
import org.jamon.node.ParamValueNode;
import org.jamon.node.PathElementNode;
import org.jamon.node.RelativePathNode;
import org.jamon.node.SimpleCallNode;
import org.jamon.node.TextNode;
import org.jamon.node.UnnamedFragmentNode;
import org.jamon.node.UnnamedParamsNode;
import org.junit.Test;

/**
 * @author ian
 **/
public class CallParserTest extends AbstractParserTest {
  @Override
  protected AbstractNode parse(String text) throws IOException {
    final PositionalPushbackReader reader = makeReader(text);
    assertEquals('<', (char) reader.read());
    assertEquals('&', (char) reader.read());
    ParserErrorsImpl errors = new ParserErrorsImpl();
    AbstractCallNode result = new CallParser(reader, errors, START_LOC).getCallNode();
    if (errors.hasErrors()) {
      throw errors;
    }
    else {
      return result;
    }
  }

  @Test
  public void testParseBasicCall() throws Exception {
    assertEquals(
      new SimpleCallNode(START_LOC, relPathNode(1, 4, "foo"), new NoParamsNode(location(1, 8))),
      parse("<& foo &>"));
  }

  @Test
  public void testSimpleCallWithGenericParam() throws Exception {
    assertEquals(
      new SimpleCallNode(
        START_LOC,
        relPathNode(1, 4, "foo"),
        new NoParamsNode(location(1, 16)))
          .addGenericParam(new GenericCallParam(location(1, 8), "String")),
      parse("<& foo<String> &>"));
  }

  @Test
  public void testSimpleCallWithGenericParams() throws Exception {
    assertEquals(
      new SimpleCallNode(START_LOC, relPathNode(1, 4, "foo"), new NoParamsNode(location(1, 13)))
        .addGenericParam(new GenericCallParam(location(1, 8), "A"))
        .addGenericParam(new GenericCallParam(location(1, 10), "B")),
      parse("<& foo<A,B> &>"));
  }

  @Test
  public void testSimpleCallWithComplexGenericParam() throws Exception {
    assertEquals(
      new SimpleCallNode(START_LOC, relPathNode(1, 4, "foo"), new NoParamsNode(location(1, 16)))
        .addGenericParam(new GenericCallParam(location(1, 8), "A<B,C>")),
      parse("<& foo<A<B,C>> &>"));
  }

  @Test
  public void testParseNamedArgCall() throws Exception {
    assertEquals(
      new SimpleCallNode(START_LOC, absPathNode(1, 4, "foo"), new NamedParamsNode(location(1, 8))),
      parse("<& /foo; &>"));
    assertEquals(
      new SimpleCallNode(
        START_LOC,
        absPathNode(1, 4, "foo"),
        new NamedParamsNode(location(1, 8)).addParam(namedParamNode(1,10, "a", 1, 15, "\"a;\" "))),
      parse("<& /foo; a => \"a;\" &>"));
    assertEquals(
      new SimpleCallNode(
        START_LOC,
        absPathNode(1, 4, "foo"),
        new NamedParamsNode(location(1, 8)).addParam(namedParamNode(1, 10, "a", 1, 15, "\"a;\""))),
      parse("<& /foo; a => \"a;\"; &>"));
    assertEquals(
      new SimpleCallNode(
        START_LOC,
        absPathNode(1, 4, "foo"),
        new NamedParamsNode(location(1, 8))
          .addParam(namedParamNode(1, 10, "a", 1, 15, "\"a;\""))
          .addParam(namedParamNode(1, 21, "b", 1, 26, "'&' "))),
      parse("<& /foo; a => \"a;\"; b => '&' &>"));
  }

  @Test
  public void testNamedArgCallWithGenericParam() throws Exception {
    assertEquals(
      new SimpleCallNode(
        START_LOC,
        absPathNode(1, 4, "foo"),
        new NamedParamsNode(location(1, 11)).addParam(namedParamNode(1, 13, "a", 1, 18, "1 ")))
        .addGenericParam(new GenericCallParam(location(1, 9), "A")),
      parse("<& /foo<A>; a => 1 &>"));
  }

  @Test
  public void testParseUnnamedArgCall() throws Exception {
    assertEquals(
      new SimpleCallNode(START_LOC,
        absPathNode(1, 4, "foo"),
        new UnnamedParamsNode(location(1, 8))),
      parse("<& /foo: &>"));
    assertEquals(
      new SimpleCallNode(
        START_LOC,
        absPathNode(1, 4, "foo"),
        new UnnamedParamsNode(location(1, 8))
          .addValue(new ParamValueNode(location(1, 10), "\"a;\" "))),
      parse("<& /foo: \"a;\" &>"));
    assertEquals(
      new SimpleCallNode(
        START_LOC,
        absPathNode(1, 4, "foo"),
        new UnnamedParamsNode(location(1, 8))
          .addValue(new ParamValueNode(location(1, 10), "\"a;\""))),
      parse("<& /foo: \"a;\"; &>"));
    assertEquals(
      new SimpleCallNode(
        START_LOC,
        absPathNode(1, 4, "foo"),
        new UnnamedParamsNode(location(1, 8))
          .addValue(new ParamValueNode(location(1, 10), "\"a;\""))
          .addValue(new ParamValueNode(location(1, 16), "'&' "))),
      parse("<& /foo: \"a;\"; '&' &>"));
    assertEquals(
      new SimpleCallNode(
        START_LOC,
        absPathNode(1, 4, "foo"),
        new UnnamedParamsNode(location(1, 8))
          .addValue(new ParamValueNode(location(1, 10), "x && a > b "))),
      parse("<& /foo: x && a > b &>"));
  }

  @Test
  public void testUnnamedArgCallWithGenericParam() throws Exception {
    assertEquals(
      new SimpleCallNode(
        START_LOC,
        absPathNode(1, 4, "foo"),
        new UnnamedParamsNode(location(1, 11))
          .addValue(new ParamValueNode(location(1, 13), "1 ")))
        .addGenericParam(new GenericCallParam(location(1, 9), "A")),
      parse("<& /foo<A>: 1 &>"));
  }

  @Test
  public void testChildCall() throws IOException {
    String call = "<& *CHILD &>";
    assertEquals(new ChildCallNode(START_LOC), parse(call));
  }

  @Test
  public void testFragmentCall() throws Exception {
    assertEquals(
      new FragmentCallNode(
        START_LOC,
        absPathNode(1, 5, "foo"),
        new UnnamedParamsNode(location(1, 9))
          .addValue(new ParamValueNode(location(1, 11), "3 ")),
        (UnnamedFragmentNode) new UnnamedFragmentNode(location(1, 15))
          .addSubNode(new TextNode(location(1, 15), "bar"))),
      parse("<&| /foo: 3 &>bar</&>"));
  }

  private static String MULTI_FRAGMENT_START = "<&|| foo &>\n";

  private static String FRAGMENTS_END = "</&>";

  private static MultiFragmentCallNode makeMultiFragmentCall() {
    return new MultiFragmentCallNode(
      START_LOC,
      relPathNode(1, 6, "foo"),
      new NoParamsNode(location(1, 10)));
  }

  @Test
  public void testMultiFragmentCall() throws Exception {
    assertEquals(makeMultiFragmentCall(), parse(MULTI_FRAGMENT_START + FRAGMENTS_END));
    assertEquals(
      makeMultiFragmentCall()
        .addFragment(
          (NamedFragmentNode) new NamedFragmentNode(location(2, 1), "bar")
            .addSubNode(new TextNode(location(2, 7), "baz"))),
      parse(MULTI_FRAGMENT_START + "<|bar>baz</|>" + FRAGMENTS_END));
    assertEquals(
      makeMultiFragmentCall()
        .addFragment(
          (NamedFragmentNode) new NamedFragmentNode(location(2, 1), "bar")
            .addSubNode(new TextNode(location(2, 7), "baz")))
        .addFragment(
          (NamedFragmentNode) new NamedFragmentNode(location(3, 1), "bob")
            .addSubNode(new TextNode(location(3, 7), "joe"))),
      parse(MULTI_FRAGMENT_START + "<|bar>baz</|>\n<|bob>joe</|>" + FRAGMENTS_END));
  }

  @Test
  public void testMaformedCallTag() throws Exception {
    assertError("<&foo; a b", 1, 10, CallParser.MISSING_ARG_ARROW_ERROR);
    assertError("<& foo a", 1, 8, CallParser.GENERIC_ERROR);
    assertError("<&| foo &>bob", 1, 11, UnnamedFragmentParser.FRAGMENT_CLOSE_EXPECTED);
    assertError("<& foo:", 1, 8, CallParser.PARAM_VALUE_EOF_ERROR);
    assertError("<&|| foo &><|foo></|>", 1, 12, CallParser.FRAGMENTS_EOF_ERROR);
    assertErrorPair(
      "<&|| foo &>\n<|bar>123",
      2, 7, NamedFragmentParser.NAMED_FRAGMENT_CLOSE_EXPECTED,
      1, 12, CallParser.FRAGMENTS_EOF_ERROR);
    assertError("<&|| foo &>\nbob", 2, 1, CallParser.UNEXPECTED_IN_MULTI_FRAG_ERROR);
    assertError("<&|| foo &>\n<bob", 2, 2, CallParser.UNEXPECTED_IN_MULTI_FRAG_ERROR);
    assertError("<&|| foo &>\n<", 2, 2, CallParser.UNEXPECTED_IN_MULTI_FRAG_ERROR);
    assertError("<&|| foo &>\n<|bob", 2, 5, CallParser.UNEXPECTED_IN_MULTI_FRAG_ERROR);
    assertError("<&|| foo &>\n<|></|></&>", 2, 3, AbstractParser.NOT_AN_IDENTIFIER_ERROR);
  }

  @Test
  public void testMissingGenericCallParamClose() throws Exception {
    assertError("<& foo<x\n&>", 2, 1, CallParser.MISSING_GENERIC_PARAM_CLOSE_ERROR);
  }

  @Test
  public void testAsterixNonChildCall() throws Exception {
    assertError("<& *NOTCHILD &>", 1, 4, CallParser.INVALID_CALL_TARGET_ERROR);
  }

  @Test
  public void testMalformedChildCall() throws Exception {
    assertError("<& *CHILD foo", 1, 11, CallParser.MISSING_CALL_CLOSE_ERROR);
  }

  private static RelativePathNode relPathNode(int line, int column, String pathElement) {
    return (RelativePathNode) new RelativePathNode(location(line, column))
      .addPathElement(new PathElementNode(location(line, column), "foo"));
  }

  private static AbsolutePathNode absPathNode(int line, int column, String pathElement) {
    return (AbsolutePathNode) new AbsolutePathNode(location(line, column))
      .addPathElement(new PathElementNode(location(line, column + 1), pathElement));
  }

  private static NamedParamNode namedParamNode(
    int nameLine, int nameColumn, String name, int valueLine, int valueColumn, String value) {
    return new NamedParamNode(
      location(nameLine, nameColumn),
      new ParamNameNode(location(nameLine, nameColumn), name),
      new ParamValueNode(location(valueLine, valueColumn), value));
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(CallParserTest.class);
  }
}
