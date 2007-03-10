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
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.util.List;
import java.util.Iterator;

import org.jamon.ParserError;
import org.jamon.node.Location;

public class UnnamedParamValues implements ParamValues
{
    public UnnamedParamValues(List<String> p_params, Location p_location)
    {
        m_params = p_params;
        m_location = p_location;
    }

    public void generateRequiredArgs(
        Iterator<RequiredArgument> p_args, CodeWriter p_writer)
        throws ParserError
    {
        int numberOfRequiredArgs = 0;
        while(p_args.hasNext())
        {
            numberOfRequiredArgs++;
            p_args.next();
        }

        if (numberOfRequiredArgs != m_params.size())
        {
            throw new ParserError(
                m_location,
                "Call provides " + m_params.size() + " arguments when "
                + numberOfRequiredArgs + " are expected");
        }
        for (String param : m_params)
        {
            p_writer.printListElement(param);
        }
    }

    public String getOptionalArgValue(String p_argName)
    {
        return null;
    }

    public boolean hasUnusedParams()
    {
        return false;
    }

    public Iterator<String> getUnusedParams()
    {
        throw new IllegalStateException();
    }


    private final List<String> m_params;
    private final Location m_location;
}
