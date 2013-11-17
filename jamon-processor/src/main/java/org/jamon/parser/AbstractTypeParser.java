/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;

public class AbstractTypeParser extends AbstractParser {
  public static final String UNEXPECTED_ARRAY_ERROR = "Arrays not allowed in this context";

  public AbstractTypeParser(
    Location location, PositionalPushbackReader reader, ParserErrorsImpl errors)
  throws IOException, ParserErrorImpl {
    super(reader, errors);
    try {
      parseComponent();
      while (readAndAppendChar('.', typeBuilder)) {
        soakWhitespace();
        parseComponent();
      }
    }
    catch (NotAnIdentifierException e) {
      throw new ParserErrorImpl(location, BAD_JAVA_TYPE_SPECIFIER);
    }
    checkForArrayBrackets();
  }

  private void parseComponent() throws IOException, NotAnIdentifierException, ParserErrorImpl {
    typeBuilder.append(readIdentifierOrThrow());
    soakWhitespace();
    parseTypeElaborations();
  }

  /**
   * @throws IOException
   * @throws NotAnIdentifierException
   * @throws ParserErrorImpl
   */
  protected void parseTypeElaborations() throws IOException,
    NotAnIdentifierException,
    ParserErrorImpl {}

  protected void checkForArrayBrackets() throws IOException, ParserErrorImpl {
    if (readChar('[')) {
      throw new ParserErrorImpl(reader.getLocation(), UNEXPECTED_ARRAY_ERROR);
    }
  }

  public String getType() {
    return typeBuilder.toString();
  }

  protected final StringBuilder typeBuilder = new StringBuilder();
}
