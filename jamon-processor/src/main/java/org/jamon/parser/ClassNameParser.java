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
