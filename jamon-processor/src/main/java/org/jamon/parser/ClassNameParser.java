/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;

public class ClassNameParser extends AbstractTypeParser {
  public ClassNameParser(
    Location location, PositionalPushbackReader reader, ParserErrorsImpl errors)
  throws IOException, ParserErrorImpl {
    super(location, reader, errors);
  }

  private void readGenericsParameter() throws IOException,
    NotAnIdentifierException,
    ParserErrorImpl {
    boolean boundsAllowed;
    if (readAndAppendChar('?', typeBuilder)) {
      boundsAllowed = true;
    }
    else {
      // FIXME - check for errors
      String type = new TypeNameParser(reader.getLocation(), reader, errors).getType();
      typeBuilder.append(type);
      boundsAllowed = (type.indexOf('.') < 0);
    }
    if (boundsAllowed && soakWhitespace()) {
      readBoundingType();
    }
  }

  @Override
  protected void parseTypeElaborations() throws IOException,
    NotAnIdentifierException,
    ParserErrorImpl {
    int c = reader.read();
    if (c != '<') {
      reader.unread(c);
    }
    else {
      c = reader.read();
      reader.unread(c);
      if (c == '/' || c == '%') // looks like a jamon tag
      {
        reader.unread('<');
      }
      else {
        typeBuilder.append('<');
        soakWhitespace();
        readGenericsParameter();
        soakWhitespace();
        while (readAndAppendChar(',', typeBuilder)) {
          soakWhitespace();
          readGenericsParameter();
          soakWhitespace();
        }
        if (!readAndAppendChar('>', typeBuilder)) {
          throw new NotAnIdentifierException();
        }
      }
    }
  }

  protected void readBoundingType() throws IOException, NotAnIdentifierException, ParserErrorImpl {
    boolean needBoundingType = false;
    if (readChar('e')) {
      if (checkToken("xtends") && soakWhitespace()) {
        typeBuilder.append(" extends ");
        needBoundingType = true;
      }
      else {
        throw new NotAnIdentifierException();
      }
    }
    else if (readChar('s')) {
      if (checkToken("uper") && soakWhitespace()) {
        typeBuilder.append(" super ");
        needBoundingType = true;
      }
      else {
        throw new NotAnIdentifierException();
      }
    }
    if (needBoundingType) {
      typeBuilder.append(new TypeNameParser(reader.getLocation(), reader, errors).getType());
    }
  }
}
