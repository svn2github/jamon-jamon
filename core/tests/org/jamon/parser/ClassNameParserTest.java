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

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.node.Location;
import org.junit.Test;

public class ClassNameParserTest extends AbstractClassNameParserTest
{
    @Test public void testUnexpectedArray() throws Exception
    {
        assertError("foo[]", 1, 4, AbstractTypeParser.UNEXPECTED_ARRAY_ERROR);
    }

    @Override
    protected ClassNameParser makeParser(Location p_location,
        PositionalPushbackReader p_reader,
        ParserErrors p_errors) throws IOException, ParserError
    {
        return new ClassNameParser(p_location, p_reader, p_errors);
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.JUnit4TestAdapter(ClassNameParserTest.class);
    }
}
