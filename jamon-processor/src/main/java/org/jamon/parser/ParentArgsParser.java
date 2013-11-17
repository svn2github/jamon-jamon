/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgValueNode;
import org.jamon.node.ParentArgNode;
import org.jamon.node.ParentArgWithDefaultNode;
import org.jamon.node.ParentArgsNode;

public final class ParentArgsParser extends AbstractParser {
  private final ParentArgsNode parentArgsNode;

  public static final String MALFORMED_PARENT_ARGS_CLOSE = "Expecting parent arg declaration or '</%xargs>'";

  public ParentArgsParser(
    PositionalPushbackReader reader, ParserErrorsImpl errors, Location tagLocation)
  throws IOException {
    super(reader, errors);
    parentArgsNode = new ParentArgsNode(tagLocation);
    if (checkForTagClosure(tagLocation)) {
      while (true) {
        soakWhitespace();
        if (readChar('<')) {
          org.jamon.api.Location location = reader.getLocation();
          if (!checkToken("/%xargs>")) {
            addError(location, MALFORMED_PARENT_ARGS_CLOSE);
          }
          soakWhitespace();
          return;
        }
        else {
          try {
            handleParentArg(parentArgsNode);
          }
          catch (ParserErrorImpl e) {
            addError(e);
            return;
          }
        }
      }
    }
  }

  public ParentArgsNode getParentArgsNode() {
    return parentArgsNode;
  }

  private void handleParentArg(ParentArgsNode parentArgsNode)
  throws IOException, ParserErrorImpl {
    ArgNameNode argName = new ArgNameNode(reader.getNextLocation(), readIdentifier(true));
    soakWhitespace();
    if (readChar(';')) {
      parentArgsNode.addArg(new ParentArgNode(argName.getLocation(), argName));
    }
    else if (readChar('=')) {
      readChar('>'); // support old-style syntax
      soakWhitespace();
      org.jamon.api.Location valueLocation = reader.getNextLocation();
      parentArgsNode.addArg(new ParentArgWithDefaultNode(
        argName.getLocation(),
        argName,
        new ArgValueNode(
          valueLocation, readJava(valueLocation, new OptionalValueTagEndDetector()))));
    }
    else {
      throw new ParserErrorImpl(reader.getNextLocation(),
          OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
    }
  }

}
