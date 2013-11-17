/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.GenericsParamNode;
import org.jamon.node.GenericsBoundNode;
import org.jamon.node.GenericsNode;

public class GenericsParser extends AbstractParser {
  final static String EXPECTING_EXTENDS_OR_GENERIC_ERROR = "expecting ',', 'extends' or '</%generic>";

  final static String TYPE_PARAMETER_EXPECTED_ERROR = "type parameter expected";

  final static String EXPECTING_GENERIC_ERROR = "expecting '</%generic>'";

  public GenericsParser(
    PositionalPushbackReader reader, ParserErrorsImpl errors, Location tagLocation)
  throws IOException {
    super(reader, errors);
    genericsNode = new GenericsNode(tagLocation);
    checkForTagClosure(tagLocation);
    while (true) {
      soakWhitespace();
      reader.markNodeEnd();
      String paramName = null;
      try {
        paramName = readIdentifierOrThrow();
      }
      catch (NotAnIdentifierException e) {
        addError(reader.getCurrentNodeLocation(), TYPE_PARAMETER_EXPECTED_ERROR);
        return;
      }

      GenericsParamNode param = new GenericsParamNode(reader.getCurrentNodeLocation(), paramName);
      genericsNode.addParam(param);
      soakWhitespace();
      int c = reader.read();

      switch (c) {
        case ',':
          break;
        case '<':
          if (!checkToken("/%generic>")) {
            addError(reader.getLocation(), EXPECTING_EXTENDS_OR_GENERIC_ERROR);
          }
          soakWhitespace();
          return;
        case 'e':
          if (checkToken("xtends")) {
            if (!soakWhitespace()) {
              addError(reader.getLocation(), EXPECTING_EXTENDS_OR_GENERIC_ERROR);
              return;
            }
            boolean readingBounds = true;
            while (readingBounds) {
              reader.markNodeEnd();
              String bound = readClassName(reader.getNextLocation());
              if (bound.length() == 0) {
                return;
              }
              else {
                param.addBound(new GenericsBoundNode(reader.getCurrentNodeLocation(), bound));
              }
              soakWhitespace();
              readingBounds = readChar('&');
              if (readingBounds) {
                soakWhitespace();
              }
            }
            if (!readChar(',')) {
              if (!checkToken("</%generic>")) {
                addError(reader.getLocation(), EXPECTING_GENERIC_ERROR);
              }
              soakWhitespace();
              return;
            }
          }
          else {
            addError(reader.getLocation(), EXPECTING_EXTENDS_OR_GENERIC_ERROR);
            return;
          }
          break;
        default:
          addError(reader.getLocation(), EXPECTING_EXTENDS_OR_GENERIC_ERROR);
          return;
      }
    }
  }

  public GenericsNode getGenericsNode() {
    return genericsNode;
  }

  private final GenericsNode genericsNode;
}
