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

import org.jamon.node.Token;

public class UnnamedParamValues implements ParamValues
{
    public UnnamedParamValues(List p_params,
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
        int numberOfRequiredArgs = 0;
        while(p_args.hasNext())
        {
            numberOfRequiredArgs++;
            p_args.next();
        }

        if (numberOfRequiredArgs != m_params.size())
        {
            throw new AnalysisException(
                "Call provides " + m_params.size() + " arguments when "
                + numberOfRequiredArgs + " are expected",
                m_templateIdentifier,
                m_token);
        }
        for (Iterator i = m_params.iterator(); i.hasNext(); )
        {
            p_writer.printArg(i.next());
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

    public Iterator getUnusedParams()
    {
        throw new IllegalStateException();
    }


    private final List m_params;
    private final Token m_token;
    private final String m_templateIdentifier;
}
