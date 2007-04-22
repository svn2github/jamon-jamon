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

import org.jamon.ParserErrorImpl;
import org.jamon.ParserErrorsImpl;

public class ClassNameParser extends AbstractTypeParser
{
    public ClassNameParser(
        org.jamon.api.Location p_location,
        PositionalPushbackReader p_reader,
        ParserErrorsImpl p_errors) throws IOException, ParserErrorImpl
    {
        super(p_location, p_reader, p_errors);
    }

    private void readGenericsParameter() 
        throws IOException, NotAnIdentifierException, ParserErrorImpl
    {
        boolean boundsAllowed;
        if (readAndAppendChar('?', m_type))
        {
            boundsAllowed = true;
        }
        else
        {
            //FIXME - check for errors
            String type = new TypeNameParser(
                m_reader.getLocation(), m_reader, m_errors).getType();
            m_type.append(type);
            boundsAllowed = (type.indexOf('.') < 0);
        }
        if (boundsAllowed && soakWhitespace())
        {
            readBoundingType();
        }
    }
    
    @Override
    protected void parseTypeElaborations()
        throws IOException, NotAnIdentifierException, ParserErrorImpl
    {
        int c = m_reader.read();
        if (c != '<')
        {
            m_reader.unread(c);
        }
        else
        {
            c = m_reader.read();
            m_reader.unread(c);
            if (c == '/' || c == '%') // looks like a jamon tag
            {
                m_reader.unread('<');
            }
            else
            {
                m_type.append('<');
                soakWhitespace();
                readGenericsParameter();
                soakWhitespace();
                while(readAndAppendChar(',', m_type))
                {
                    soakWhitespace();
                    readGenericsParameter();
                    soakWhitespace();
                }
                if (!readAndAppendChar('>', m_type))
                {
                    throw new NotAnIdentifierException();
                }
            }
        }
    }

    protected void readBoundingType()
        throws IOException, NotAnIdentifierException, ParserErrorImpl
    {
        boolean needBoundingType = false;
        if (readChar('e'))
        {
            if (checkToken("xtends") && soakWhitespace())
            {
                m_type.append(" extends ");
                needBoundingType = true;
            }
            else
            {
                throw new NotAnIdentifierException();
            }
        }
        else if (readChar('s'))
        {
            if (checkToken("uper") && soakWhitespace())
            {
                m_type.append(" super ");
                needBoundingType = true;
            }
            else
            {
                throw new NotAnIdentifierException();
            }
        }
        if (needBoundingType)
        {
            m_type.append(new TypeNameParser(
                m_reader.getLocation(), m_reader, m_errors).getType());
        }
    }
}
