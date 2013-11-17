/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.AbstractCallNode;
import org.jamon.node.AbstractComponentCallNode;
import org.jamon.node.AbstractParamsNode;
import org.jamon.node.AbstractPathNode;
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
import org.jamon.node.SimpleCallNode;
import org.jamon.node.UnnamedFragmentNode;
import org.jamon.node.UnnamedParamsNode;

public class CallParser extends AbstractParser {
  public static final String INVALID_CALL_TARGET_ERROR = "Invalid call target";
  public static final String MISSING_CALL_CLOSE_ERROR = "Expecting '&>'";
  public static final String UNEXPECTED_IN_MULTI_FRAG_ERROR =
    "Expecting either '<|identifier>' or '</&>'";
  public static final String MISSING_ARG_ARROW_ERROR =
    "Expecting '=' or '=>' to separate param name and value";
  public static final String GENERIC_ERROR = "Malformed call tag";
  public static final String PARAM_VALUE_EOF_ERROR =
    "Reached end of file while reading parameter value";
  public static final String FRAGMENTS_EOF_ERROR =
    "Reached end of file while reading call fragments; '</&>' Expected";
  public static final String MISSING_GENERIC_PARAM_CLOSE_ERROR = "Expecing ',' or '>'";

  public CallParser(
    PositionalPushbackReader reader, ParserErrorsImpl errors, Location callStartLocation)
  throws IOException {
    super(reader, errors);
    try {
      if (readChar('|')) {
        if (readChar('|')) {
          callNode = parseNamedFragmentCall(callStartLocation);
        }
        else {
          callNode = parseUnnamedFragmentCall(callStartLocation);
        }
        addGenericParams();
      }
      else {
        soakWhitespace();
        if (readChar('*')) {
          parseChildCall(callStartLocation);
        }
        else {
          AbstractPathNode path = parsePath();
          parseGenericParams();
          callNode = new SimpleCallNode(callStartLocation, path, parseParams());
          addGenericParams();
        }
      }
    }
    catch (ParserErrorImpl e) {
      addError(e);
      callNode = new SimpleCallNode(callStartLocation,
          new AbsolutePathNode(callStartLocation), new NoParamsNode(reader.getLocation()));
    }
  }

  private void parseChildCall(Location callStartLocation) throws IOException, ParserErrorImpl {
    Location callTargetLocation = reader.getLocation();
    if (checkToken("CHILD")) {
      soakWhitespace();
      Location endLocation = reader.getNextLocation();
      if (checkToken("&>")) {
        callNode = new ChildCallNode(callStartLocation);
      }
      else {
        throw new ParserErrorImpl(endLocation, MISSING_CALL_CLOSE_ERROR);
      }
    }
    else {
      throw new ParserErrorImpl(callTargetLocation, INVALID_CALL_TARGET_ERROR);
    }
  }

  private AbstractComponentCallNode parseNamedFragmentCall(Location callStartLocation)
  throws IOException, ParserErrorImpl {
    soakWhitespace();
    AbstractPathNode path = parsePath();
    parseGenericParams();
    MultiFragmentCallNode callNode = new MultiFragmentCallNode(callStartLocation, path,
        parseParams());
    Location fragmentsStart = reader.getNextLocation();
    while (true) {
      soakWhitespace();
      Location fragmentStart = reader.getNextLocation();
      int c = reader.read();
      if (c == '<') {
        switch (reader.read()) {
          case '|':
            String name = readIdentifier(true);
            if (readChar('>')) {
              NamedFragmentNode fragmentNode = new NamedFragmentNode(fragmentStart, name);
              new NamedFragmentParser(fragmentNode, reader, errors).parse();
              callNode.addFragment(fragmentNode);
            }
            else {
              throw new ParserErrorImpl(reader.getLocation(), UNEXPECTED_IN_MULTI_FRAG_ERROR);
            }
            break;
          case '/':
            if (readChar('&') && readChar('>')) {
              return callNode;
            }
            else {
              throw new ParserErrorImpl(reader.getLocation(), UNEXPECTED_IN_MULTI_FRAG_ERROR);
            }
          default:
            throw new ParserErrorImpl(reader.getLocation(), UNEXPECTED_IN_MULTI_FRAG_ERROR);
        }
      }
      else if (c >= 0) {
        throw new ParserErrorImpl(reader.getLocation(), UNEXPECTED_IN_MULTI_FRAG_ERROR);
      }
      else {
        throw new ParserErrorImpl(fragmentsStart, FRAGMENTS_EOF_ERROR);
      }
    }
  }

  private FragmentCallNode parseUnnamedFragmentCall(Location callStartLocation) throws IOException,
    ParserErrorImpl {
    soakWhitespace();
    AbstractPathNode path = parsePath();
    parseGenericParams();
    AbstractParamsNode params = parseParams();
    return new FragmentCallNode(callStartLocation, path, params, new UnnamedFragmentParser(
        new UnnamedFragmentNode(reader.getNextLocation()), reader, errors).parse().getRootNode());
  }

  private AbstractParamsNode parseParams() throws IOException, ParserErrorImpl {
    soakWhitespace();
    reader.markNodeEnd();
    int c = reader.read();
    switch (c) {
      case '&':
        if (reader.read() == '>') {
          return new NoParamsNode(reader.getCurrentNodeLocation());
        }
        else {
          throw new ParserErrorImpl(reader.getLocation(), GENERIC_ERROR);
        }
      case ';':
        return parseNamedParams();
      case ':':
        return parseUnnamedParams();
      default:
        throw new ParserErrorImpl(reader.getLocation(), GENERIC_ERROR);
    }
  }

  private static class ParamValueEndDetector implements TagEndDetector {
    public boolean noMoreParams() {
      return noMoreParams;
    }

    @Override
    public int checkEnd(final char character) {
      if (character == '&') {
        seenAmpersand = true;
        return 0;
      }
      else if (character == '>' && seenAmpersand) {
        noMoreParams = true;
        return 2;
      }
      else if (character == ';') {
        return 1;
      }
      else {
        seenAmpersand = false;
        return 0;
      }
    }

    @Override
    public ParserErrorImpl getEofError(Location startLocation) {
      return new ParserErrorImpl(startLocation, PARAM_VALUE_EOF_ERROR);
    }

    @Override
    public void resetEndMatch() {
      seenAmpersand = false;
    }

    private boolean noMoreParams = false;
    private boolean seenAmpersand = false;
  }

  private NamedParamsNode parseNamedParams() throws ParserErrorImpl, IOException {
    NamedParamsNode params = new NamedParamsNode(reader.getLocation());
    ParamValueEndDetector endDetector = new ParamValueEndDetector();
    while (true) {
      soakWhitespace();
      if (readChar('&')) {
        if (readChar('>')) {
          return params;
        }
        else {
          throw new ParserErrorImpl(reader.getCurrentNodeLocation(), GENERIC_ERROR);
        }
      }
      Location nameLoc = reader.getNextLocation();
      String name = readIdentifier(true);
      readArrow();
      Location javaLoc = reader.getNextLocation();
      params.addParam(new NamedParamNode(nameLoc, new ParamNameNode(nameLoc, name),
          new ParamValueNode(javaLoc, readJava(javaLoc, endDetector))));
      if (endDetector.noMoreParams()) {
        return params;
      }
    }
  }

  private UnnamedParamsNode parseUnnamedParams() throws ParserErrorImpl, IOException {
    UnnamedParamsNode params = new UnnamedParamsNode(reader.getLocation());
    ParamValueEndDetector endDetector = new ParamValueEndDetector();
    while (true) {
      soakWhitespace();
      if (readChar('&')) {
        if (readChar('>')) {
          return params;
        }
        else {
          throw new ParserErrorImpl(reader.getCurrentNodeLocation(), GENERIC_ERROR);
        }
      }
      Location javaLoc = reader.getNextLocation();
      params.addValue(new ParamValueNode(javaLoc, readJava(javaLoc, endDetector)));
      if (endDetector.noMoreParams()) {
        return params;
      }
    }
  }

  private void parseGenericParams() throws ParserErrorImpl, IOException {
    genericParams = new ArrayList<GenericCallParam>();
    if (readChar('<')) {
      do {
        soakWhitespace();
        Location location = reader.getNextLocation();
        genericParams.add(new GenericCallParam(location, new ClassNameParser(location, reader,
            errors).getType()));
        soakWhitespace();
      }
      while (readChar(','));
      if (!readChar('>')) {
        throw new ParserErrorImpl(reader.getNextLocation(), MISSING_GENERIC_PARAM_CLOSE_ERROR);
      }
    }
  }

  private void addGenericParams() {
    AbstractComponentCallNode componentCallNode = (AbstractComponentCallNode) callNode;
    for (GenericCallParam param : genericParams) {
      componentCallNode.addGenericParam(param);
    }
  }

  private void readArrow() throws ParserErrorImpl, IOException {
    soakWhitespace();
    if (!readChar('=')) {
      throw new ParserErrorImpl(reader.getNextLocation(), MISSING_ARG_ARROW_ERROR);
    }
    readChar('>'); // support old-style syntax
    soakWhitespace();
  }

  public static void main(String[] args) {}

  public AbstractCallNode getCallNode() {
    return callNode;
  }

  private AbstractCallNode callNode;
  private List<GenericCallParam> genericParams = null;
}
