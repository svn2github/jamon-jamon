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

public class AbstractTypeParser extends AbstractParser
{
    public static final String UNEXPECTED_ARRAY_ERROR =
        "Arrays not allowed in this context";

    public AbstractTypeParser(
        Location p_location,
        PositionalPushbackReader p_reader,
        ParserErrors p_errors) throws IOException, ParserError
    {
        super(p_reader, p_errors);
        try
        {
            parseComponent();
            while (readAndAppendChar('.', m_type))
            {
                soakWhitespace();
                parseComponent();
            }
        }
        catch (NotAnIdentifierException e)
        {
            throw new ParserError(p_location, BAD_JAVA_TYPE_SPECIFIER);
        }
        checkForArrayBrackets();
    }

    private void parseComponent()
        throws IOException, NotAnIdentifierException, ParserError
    {
        m_type.append(readIdentifierOrThrow());
        soakWhitespace();
        parseTypeElaborations();
    }

    protected void parseTypeElaborations()
        throws IOException, NotAnIdentifierException, ParserError
    {
    }

    protected void checkForArrayBrackets() throws IOException, ParserError
    {
        if (readChar('['))
        {
            throw new ParserError(m_reader.getLocation(), UNEXPECTED_ARRAY_ERROR);
        }
    }

    public String getType()
    {
        return m_type.toString();
    }

    protected final StringBuilder m_type = new StringBuilder();
}
