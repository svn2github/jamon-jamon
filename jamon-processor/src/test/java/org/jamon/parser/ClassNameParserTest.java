/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.junit.Test;

public class ClassNameParserTest extends AbstractClassNameParserTest {
  @Test
  public void testUnexpectedArray() throws Exception {
    assertError("foo[]", 1, 4, AbstractTypeParser.UNEXPECTED_ARRAY_ERROR);
  }

  @Override
  protected ClassNameParser makeParser(
    Location location, PositionalPushbackReader reader, ParserErrorsImpl errors) throws IOException,
    ParserErrorImpl {
    return new ClassNameParser(location, reader, errors);
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(ClassNameParserTest.class);
  }
}
