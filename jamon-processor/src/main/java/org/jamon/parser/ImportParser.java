/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractImportNode;
import org.jamon.node.ImportNode;
import org.jamon.node.StaticImportNode;

public class ImportParser extends AbstractParser {

  public static final String MISSING_WHITESPACE_AFTER_STATIC_DECLARATION =
    "missing whitespace after static declaration";

  public ImportParser(Location location, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(reader, errors);
    this.location = location;
  }

  public ImportParser(PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(reader, errors);
    location = reader.getNextLocation();
  }

  public ImportParser parse() throws IOException, ParserErrorImpl {
    StringBuilder builder = new StringBuilder();
    try {
      String firstComponent = readIdentifierOrThrow();
      if ("static".equals(firstComponent)) {
        isStatic = true;
        if (!soakWhitespace()) {
          throw new ParserErrorImpl(location, MISSING_WHITESPACE_AFTER_STATIC_DECLARATION);
        }
        firstComponent = readIdentifierOrThrow();
      }
      soakWhitespace();
      builder.append(firstComponent);
      while (readAndAppendChar('.', builder)) {
        soakWhitespace();
        if (readAndAppendChar('*', builder)) {
          break;
        }
        builder.append(readIdentifierOrThrow());
        soakWhitespace();
      }
      importText = builder.toString();
      return this;
    }
    catch (NotAnIdentifierException e) {
      throw new ParserErrorImpl(location, BAD_JAVA_TYPE_SPECIFIER);
    }
  }

  public AbstractImportNode getNode() {
    return isStatic
        ? new StaticImportNode(location, importText)
        : new ImportNode(location, importText);
  }

  public boolean isStatic() {
    return isStatic;
  }

  private final Location location;
  private String importText;
  private boolean isStatic = false;
}
