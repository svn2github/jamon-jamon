/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;

public class TypeNameParser extends ClassNameParser {
  public TypeNameParser(Location location, PositionalPushbackReader reader, ParserErrorsImpl errors)
  throws IOException, ParserErrorImpl {
    super(location, reader, errors);
  }

  @Override
  protected void checkForArrayBrackets() throws IOException {
    while (readChar('[')) {
      soakWhitespace();
      if (!readChar(']')) {
        addError(reader.getNextLocation(), INCOMPLETE_ARRAY_SPECIFIER_ERROR);
        return;
      }
      typeBuilder.append("[]");
      soakWhitespace();
    }
  }
}
