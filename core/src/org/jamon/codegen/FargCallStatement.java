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
 * Contributor(s):
 */

package org.jamon.codegen;

import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import org.jamon.JamonException;

public class FargCallStatement
    implements Statement
{
    FargCallStatement(String p_path,Map p_params, FargInfo p_fargInfo)
    {
        m_path = p_path;
        m_params = p_params;
        m_fargInfo = p_fargInfo;
    }

    private final String m_path;
    private final Map m_params;
    private final FargInfo m_fargInfo;

    public void generateSource(IndentingWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAnalyzer p_analyzer)
        throws IOException
    {
        // FIXME!
        String tn = getPath();
        p_writer.print  (tn);
        p_writer.println(".writeTo(this.getWriter());");
        p_writer.print  (tn);
        p_writer.println(".escaping(this.getEscaping());");
        p_writer.print  (tn);
        p_writer.print  (".render(");
        for (Iterator r = m_fargInfo.getRequiredArgs(); r.hasNext(); /* */)
        {
            Argument arg = (Argument) r.next();
            String name = arg.getName();
            String expr = (String) m_params.get(name);
            if (expr == null)
            {
                throw new JamonException
                    ("No value supplied for required argument " + name);
            }
            p_writer.print("(");
            p_writer.print(expr);
            p_writer.print(")");
            if (r.hasNext())
            {
                p_writer.print(", ");
            }
        }
        p_writer.println(");");
    }

    private String getPath()
    {
        return m_path;
    }
}
