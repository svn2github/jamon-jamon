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

import org.jamon.ParserErrors;
import org.jamon.node.GenericsParamNode;
import org.jamon.node.GenericsBoundNode;
import org.jamon.node.GenericsNode;
import org.jamon.node.Location;

public class GenericsParser extends AbstractParser
{
    final static String EXPECTING_EXTENDS_OR_GENERIC_ERROR =
        "expecting ',', 'extends' or '</%generic>";
    final static String TYPE_PARAMETER_EXPECTED_ERROR =
        "type parameter expected";
    final static String EXPECTING_GENERIC_ERROR = "expecting '</%generic>'";

    public GenericsParser(PositionalPushbackReader p_reader,
                          ParserErrors p_errors,
                          Location p_tagLocation) throws IOException
    {
        super(p_reader, p_errors);
        m_genericsNode = new GenericsNode(p_tagLocation);
        checkForTagClosure(p_tagLocation);
        while (true)
        {
            soakWhitespace();
            m_reader.markNodeEnd();
            String paramName = null;
            try
            {
                paramName = readIdentifierOrThrow();
            }
            catch (NotAnIdentifierException e)
            {
                addError(m_reader.getCurrentNodeLocation(),
                         TYPE_PARAMETER_EXPECTED_ERROR);
                return;
            }

            GenericsParamNode param =
               new GenericsParamNode(
                   m_reader.getCurrentNodeLocation(), paramName);
            m_genericsNode.addParam(param);
            soakWhitespace();
            int c = m_reader.read();

            switch (c)
            {
            case ',': break;
            case '<':
                if (!checkToken("/%generic>"))
                {
                    addError(m_reader.getLocation(),
                             EXPECTING_EXTENDS_OR_GENERIC_ERROR);
                }
                soakWhitespace();
                return;
            case 'e':
                if (checkToken("xtends"))
                {
                    if (!soakWhitespace())
                    {
                        addError(m_reader.getLocation(),
                                 EXPECTING_EXTENDS_OR_GENERIC_ERROR);
                        return;
                    }
                    boolean readingBounds = true;
                    while (readingBounds)
                    {
                        m_reader.markNodeEnd();
                        String bound = readClassName(m_reader.getNextLocation());
                        if (bound.length() == 0)
                        {
                            return;
                        }
                        else
                        {
                            param.addBound(new GenericsBoundNode(
                                m_reader.getCurrentNodeLocation(),
                                bound));
                        }
                        soakWhitespace();
                        readingBounds = readChar('&');
                        if (readingBounds)
                        {
                            soakWhitespace();
                        }
                    }
                    if (!readChar(','))
                    {
                        if (!checkToken("</%generic>"))
                        {
                            addError(m_reader.getLocation(),
                                     EXPECTING_GENERIC_ERROR);
                        }
                        soakWhitespace();
                        return;
                    }
                }
                else
                {
                    addError(m_reader.getLocation(),
                             EXPECTING_EXTENDS_OR_GENERIC_ERROR);
                    return;
                }
                break;
            default:
                addError(m_reader.getLocation(),
                         EXPECTING_EXTENDS_OR_GENERIC_ERROR);
                return;
            }
        }
    }

    public GenericsNode getGenericsNode()
    {
        return m_genericsNode;
    }

    private final GenericsNode m_genericsNode;
}
