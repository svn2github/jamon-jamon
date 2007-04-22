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

import org.jamon.ParserErrorsImpl;
import org.jamon.node.AbstractBodyNode;

public abstract class SubcomponentParser<Node extends AbstractBodyNode>
    extends AbstractBodyParser<Node>
{
    protected SubcomponentParser(Node p_node,
                                 PositionalPushbackReader p_reader,
                                 ParserErrorsImpl p_errors)
    {
        super(p_node, p_reader, p_errors);
    }


    @Override public AbstractBodyParser<Node> parse() throws IOException
    {
        handlePostTag();
        super.parse();
        handlePostTag();
        return this;
    }


    protected void handlePostTag() throws IOException
    {
        soakWhitespace();
    }

    protected abstract String tagName();

    @Override
    protected void handleTagClose(String p_tagName, org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (!p_tagName.equals(tagName()))
        {
            super.handleTagClose(p_tagName, p_tagLocation);
        }
        else
        {
            handlePostTag();
        }
    }

    @Override protected void handleEof()
    {
        addError(m_bodyStart, makeError(tagName()));
    }

    public static String makeError(String p_tagName)
    {
        return "Reached end of file inside a " + p_tagName +
         "; </%" + p_tagName + "> expected";
    }
}
