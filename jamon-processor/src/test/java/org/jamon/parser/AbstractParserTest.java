package org.jamon.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.jamon.api.Location;
import org.jamon.api.ParserError;
import org.jamon.api.TemplateLocation;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.compiler.TemplateFileLocation;
import org.jamon.node.AbstractNode;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgNode;
import org.jamon.node.ArgTypeNode;
import org.jamon.node.ArgValueNode;
import org.jamon.node.LocationImpl;
import org.jamon.node.OptionalArgNode;
import org.jamon.node.PathElementNode;
import org.jamon.node.TopNode;
import org.jamon.node.UpdirNode;

import static org.junit.Assert.*;

/**
 * @author ian
 **/
public abstract class AbstractParserTest {
  protected final static TemplateLocation TEMPLATE_LOC = new TemplateFileLocation("x");

  protected final static org.jamon.api.Location START_LOC = new LocationImpl(TEMPLATE_LOC, 1, 1);

  public AbstractParserTest() {}

  protected static PositionalPushbackReader makeReader(String p_text) {
    return new PositionalPushbackReader(TEMPLATE_LOC, new StringReader(p_text));
  }

  protected static TopNode topNode() {
    return new TopNode(new LocationImpl(TEMPLATE_LOC, 1, 1), "US-ASCII");
  }

  protected static org.jamon.api.Location location(int p_line, int p_column) {
    return new LocationImpl(TEMPLATE_LOC, p_line, p_column);
  }

  protected AbstractNode parse(String p_text) throws IOException {
    return new TopLevelParser(TEMPLATE_LOC, new StringReader(p_text), "US-ASCII").parse()
        .getRootNode();
  }

  protected static class PartialError {
    private final String message;

    private final int line, column;

    public PartialError(final int line, final int column, final String message) {
      this.message = message;
      this.line = line;
      this.column = column;
    }

    public ParserErrorImpl makeError() {
      return new ParserErrorImpl(new LocationImpl(TEMPLATE_LOC, line, column), message);
    }
  }

  private ParserErrorImpl makeParserErrorImpl(int line, int column, String message) {
    return new ParserErrorImpl(new LocationImpl(TEMPLATE_LOC, line, column), message);
  }

  protected void assertErrors(String body, PartialError... partialErrors) throws Exception {
    try {
      parse(body);
      fail("No failure registered for '" + body + "'");
    }
    catch (ParserErrorsImpl e) {
      List<ParserError> errors = new LinkedList<ParserError>(e.getErrors());
      List<ParserError> expected = new LinkedList<ParserError>();
      for (PartialError partialError : partialErrors) {
        expected.add(partialError.makeError());
      }
      assertEquals(expected, errors);
    }
  }

  protected void assertError(String body, int line, int column, String message) throws Exception {
    assertErrors(body, new PartialError(line, column, message));
    try {
      parse(body);
      fail("No failure registered for '" + body + "'");
    }
    catch (ParserErrorsImpl e) {
      assertEquals(Arrays.asList(makeParserErrorImpl(line, column, message)), e.getErrors());
    }
  }

  protected void assertErrorPair(
    String body,
    int line1, int column1, String message1,
    int line2, int column2, String message2) throws Exception {
    try {
      parse(body);
      fail("No failure registered for '" + body + "'");
    }
    catch (ParserErrorsImpl e) {
      assertEquals(
        Arrays.asList(
          makeParserErrorImpl(line1, column1, message1),
          makeParserErrorImpl(line2, column2, message2)),
        e.getErrors());
    }
  }

  protected void assertErrorTripple(
    String body,
    int line1, int column1, String message1,
    int line2, int column2, String message2,
    int line3, int column3, String message3) throws Exception {
    try {
      parse(body);
      fail("No failure registered for '" + body + "'");
    }
    catch (ParserErrorsImpl e) {
      assertEquals(Arrays.asList(makeParserErrorImpl(line1, column1, message1),
        makeParserErrorImpl(line2, column2, message2), makeParserErrorImpl(line3,
          column3, message3)), e.getErrors());
    }
  }

  protected static AbstractPathNode buildPath(
    Location start, AbstractPathNode path, String elements) {
    Location loc = start;
    StringTokenizer tokenizer = new StringTokenizer(elements, "/");
    while (tokenizer.hasMoreTokens()) {
      String elt = tokenizer.nextToken();
      if ("..".equals(elt)) {
        path.addPathElement(new UpdirNode(loc));
      }
      else {
        path.addPathElement(new PathElementNode(loc, elt));
      }
      loc = new LocationImpl(
        loc.getTemplateLocation(), loc.getLine(), loc.getColumn() + 1 + elt.length());
    }
    return path;
  }

  protected static ArgNode argNode(
    Location startLocation, String type, Location nameLocation, String name) {
    return new ArgNode(
      startLocation, new ArgTypeNode(startLocation, type), new ArgNameNode(nameLocation, name));
  }

  protected static OptionalArgNode optArgNode(
    Location startLocation,
    String type,
    Location nameLocation,
    String name,
    Location valueLocation,
    String value) {
      return new OptionalArgNode(
        startLocation,
        new ArgTypeNode(startLocation, type),
        new ArgNameNode(nameLocation, name),
        new ArgValueNode(valueLocation, value));
    }
}
