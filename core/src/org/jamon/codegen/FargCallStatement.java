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

import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import org.jamon.util.StringUtils;
import org.jamon.node.Token;

public class FargCallStatement
    extends AbstractStatement
    implements CallStatement
{
    FargCallStatement(String p_path,
                      Map p_params,
                      FragmentUnit p_fragmentUnit,
                      Token p_token,
                      String p_templateIdentifier)
    {
        super(p_token, p_templateIdentifier);
        m_path = p_path;
        m_params = p_params;
        m_fragmentUnit = p_fragmentUnit;
    }

    private final String m_path;
    private final Map m_params;
    private final FragmentUnit m_fragmentUnit;

    public void addFragmentImpl(FragmentUnit p_unit)
    {
        throw new UnsupportedOperationException();
    }

    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer)
        throws IOException
    {
        generateSourceLine(p_writer);
        String tn = getPath();
        p_writer.println(tn + ".writeTo(this.getWriter());");
        p_writer.println(tn + ".escapeWith(this.getEscaping());");
        p_writer.print  (tn + ".render");
        p_writer.openList();
        for (Iterator r = m_fragmentUnit.getRequiredArgs(); r.hasNext(); /* */)
        {
            RequiredArgument arg = (RequiredArgument) r.next();
            String name = arg.getName();
            String expr = (String) m_params.remove(name);
            if (expr == null)
            {
                throw new AnalysisException
                    ("No value supplied for required argument " + name,
                     getTemplateIdentifier(),
                     getToken());
            }
            p_writer.printArg("(" + expr + ")");
        }
        p_writer.closeList();
        p_writer.println(";");
        if (! m_params.isEmpty())
        {
            StringBuffer message = new StringBuffer("fragment ");
            message.append(getPath());
            message.append(" doesn't expect args ");
            StringUtils.commaJoin(message, m_params.keySet().iterator());
            throw new AnalysisException(message.toString(),
                                        getTemplateIdentifier(),
                                        getToken());
        }
    }

    private String getPath()
    {
        return m_path;
    }
}
