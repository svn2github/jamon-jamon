/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */
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
