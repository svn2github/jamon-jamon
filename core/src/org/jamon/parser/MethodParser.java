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

import org.jamon.ParserErrors;
import org.jamon.node.MethodNode;
import org.jamon.node.Location;

public class MethodParser extends SubcomponentParser<MethodNode>
{
    /**
     * @param p_reader
     * @param p_errors
     */
    public MethodParser(
        String p_name,
        Location p_tagLocation,
        PositionalPushbackReader p_reader,
        ParserErrors p_errors)
    {
        super(new MethodNode(p_tagLocation, p_name), p_reader, p_errors);
    }

    @Override protected String tagName()
    {
        return "method";
    }
}

