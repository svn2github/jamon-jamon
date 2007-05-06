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
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgValueNode;
import org.jamon.node.ParentArgNode;
import org.jamon.node.ParentArgWithDefaultNode;
import org.jamon.node.ParentArgsNode;

public final class ParentArgsParser extends AbstractParser
{
    private final ParentArgsNode m_parentArgsNode;
    public static final String MALFORMED_PARENT_ARGS_CLOSE =
    "Expecting parent arg declaration or '</%xargs>'";
    public ParentArgsParser(
        PositionalPushbackReader p_reader,
        ParserErrorsImpl p_errors,
        org.jamon.api.Location p_tagLocation) throws IOException
    {
        super(p_reader, p_errors);
        m_parentArgsNode = new ParentArgsNode(p_tagLocation);
        if (checkForTagClosure(p_tagLocation))
        {
            while(true)
            {
                soakWhitespace();
                if (readChar('<'))
                {
                    org.jamon.api.Location location = m_reader.getLocation();
                    if (!checkToken("/%xargs>"))
                    {
                        addError(location, MALFORMED_PARENT_ARGS_CLOSE);
                    }
                    soakWhitespace();
                    return;
                }
                else
                {
                    try
                    {
                        handleParentArg(m_parentArgsNode);
                    }
                    catch (ParserErrorImpl e)
                    {
                        addError(e);
                        return;
                    }
                }
            }
        }
    }

    public ParentArgsNode getParentArgsNode()
    {
        return m_parentArgsNode;
    }

    private void handleParentArg(ParentArgsNode parentArgsNode)
        throws IOException, ParserErrorImpl
    {
        ArgNameNode argName = new ArgNameNode(
            m_reader.getNextLocation(), readIdentifier(true));
        soakWhitespace();
        if (readChar(';'))
        {
            parentArgsNode.addArg(
                new ParentArgNode(argName.getLocation(), argName));
        }
        else if (readChar('='))
        {
            readChar('>'); // support old-style syntax
            soakWhitespace();
            org.jamon.api.Location valueLocation = m_reader.getNextLocation();
            parentArgsNode.addArg(
                new ParentArgWithDefaultNode(
                    argName.getLocation(),
                    argName,
                    new ArgValueNode(valueLocation,
                        readJava(
                            valueLocation,
                            new OptionalValueTagEndDetector()))));
        }
        else
        {
            throw new ParserErrorImpl(m_reader.getNextLocation(),
                OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
        }
    }

}
