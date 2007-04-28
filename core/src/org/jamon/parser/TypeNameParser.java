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

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;

public class TypeNameParser extends ClassNameParser
{
    public TypeNameParser(org.jamon.api.Location p_location,
                          PositionalPushbackReader p_reader,
                          ParserErrorsImpl p_errors) throws IOException, ParserErrorImpl
    {
        super(p_location, p_reader, p_errors);
    }

    @Override protected void checkForArrayBrackets() throws IOException
    {
        while (readChar('['))
        {
            soakWhitespace();
            if (!readChar(']'))
            {
                addError(m_reader.getNextLocation(),
                         INCOMPLETE_ARRAY_SPECIFIER_ERROR);
                return;
            }
            m_type.append("[]");
            soakWhitespace();
        }
    }
}
