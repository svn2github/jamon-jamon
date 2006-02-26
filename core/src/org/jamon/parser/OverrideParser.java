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
import org.jamon.node.OverrideNode;
import org.jamon.node.Location;

public class OverrideParser extends SubcomponentParser<OverrideNode>
{
    /**
     * @param p_reader
     * @param p_errors
     */
    public OverrideParser(
        String p_name,
        Location p_tagLocation,
        PositionalPushbackReader p_reader,
        ParserErrors p_errors)
    {
        super(new OverrideNode(p_tagLocation, p_name), p_reader, p_errors);
    }

    @Override protected String tagName()
    {
        return "override";
    }

    @Override protected void handleParentArgsNode(Location p_tagLocation)
        throws IOException
    {
        m_root.addSubNode(
            new ParentArgsParser(m_reader, m_errors, p_tagLocation)
                .getParentArgsNode());
    }
}

