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

import org.jamon.node.Token;

public class NamedParamValues implements ParamValues
{
    public NamedParamValues(Map p_params,
                            Token p_token,
                            String p_templateIdentifier)
    {
        m_params = p_params;
        m_token = p_token;
        m_templateIdentifier = p_templateIdentifier;
    }

    public void generateRequiredArgs(Iterator p_args, CodeWriter p_writer)
        throws AnalysisException
    {
        while (p_args.hasNext())
        {
            String name = ((RequiredArgument) p_args.next()).getName();
            String expr = (String) m_params.remove(name);
            if (expr == null)
            {
                throw new AnalysisException(
                    "No value supplied for required argument " + name,
                    m_templateIdentifier,
                    m_token);
            }
            p_writer.printArg(expr);
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
    private final Token m_token;
    private final String m_templateIdentifier;
}
