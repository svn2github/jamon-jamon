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
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import java.util.Map;
import java.util.Iterator;

import org.jamon.ParserError;
import org.jamon.node.Location;

public class NamedParamValues implements ParamValues
{
    public NamedParamValues(Map p_params, Location p_location)
    {
        m_params = p_params;
        m_location = p_location;
    }

    public void generateRequiredArgs(Iterator p_args, CodeWriter p_writer)
        throws ParserError
    {
        boolean multipleArgsAreMissing= false;
        StringBuffer missingArgs = null;
        while (p_args.hasNext())
        {
            String name = ((RequiredArgument) p_args.next()).getName();
            String expr = (String) m_params.remove(name);
            if (expr == null)
            {
                if (missingArgs == null)
                {
                    missingArgs = new StringBuffer(name);
                }
                else
                {
                    multipleArgsAreMissing = true;
                    missingArgs.append(", " + name);
                }
            }
            p_writer.printArg(expr);
        }
        if (missingArgs != null)
        {
            String plural = multipleArgsAreMissing ? "s" : "";
            throw new ParserError(
                m_location,
                "No value" + plural + " supplied for required argument" + plural
                + " " + missingArgs.toString());
        }
    }


    public String getOptionalArgValue(String p_argName)
    {
        return (String) m_params.remove(p_argName);
    }

    public boolean hasUnusedParams()
    {
        return ! m_params.isEmpty();
    }

    public Iterator getUnusedParams()
    {
        return m_params.keySet().iterator();
    }


    private final Map m_params;
    private final Location m_location;
}
