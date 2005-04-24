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
import org.jamon.node.Location;
import org.jamon.node.SubcomponentNode;

/**
 * @author ian
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class SubcomponentParser extends AbstractBodyParser
{
    protected SubcomponentParser(SubcomponentNode p_node, 
                                 PositionalPushbackReader p_reader,
                                 ParserErrors p_errors)
        throws IOException
    {
        super(p_node, p_reader, p_errors);
    }

    
    protected void parse() throws IOException
    {
        soakWhitespace();
        super.parse();
        soakWhitespace();
    }
    
    protected abstract String tagName();

    protected void handleTagClose(String p_tagName, Location p_tagLocation)
    throws IOException
    {
        if (!p_tagName.equals(tagName()))    
        {   
            super.handleTagClose(p_tagName, p_tagLocation);
        }
        else
        {
            soakWhitespace();
        }
    }

    protected void handleEof()
    {   
        addError(m_bodyStart, makeError(tagName()));
    }   

    public static String makeError(String p_tagName)
    {
        return "Reached end of file inside a " + p_tagName + 
         "; </%" + p_tagName + "> expected";
    }

    public SubcomponentNode getRootNode()
    {
        return (SubcomponentNode) m_root;
    }
    
}
