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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

public class CallStatement
    implements Statement
{
    CallStatement(String p_path,Map p_params)
    {
        m_path = p_path;
        m_params = p_params;
        m_fragParams = new HashMap();
    }

    public void addFragmentArg(String p_name, List p_statements)
    {
        m_fragParams.put(p_name, p_statements);
    }

    private final String m_path;
    private final Map m_fragParams;
    private final Map m_params;

    private boolean isDefCall(ImplAnalyzer p_analyzer)
    {
        return p_analyzer.getDefNames().contains(getPath());
    }

    private final static String IOEXCEPTION_CLASS =
        IOException.class.getName();


    private String getFargName(String p_fargName,
                               ImplAnalyzer p_analyzer,
                               TemplateDescriber p_describer)
        throws JamonException
    {
        if (p_fargName != null)
        {
            return p_fargName;
        }
        else if (isDefCall(p_analyzer))
        {
            return (String) p_analyzer.getFargNames(getPath()).next();
        }
        else
        {
            return (String) p_describer
                .getFargNames(p_analyzer.getAbsolutePath(getPath())).next();
        }
    }

    private void handleFragParam(String p_fargName,
                                 List p_statements,
                                 PrintWriter p_writer,
                                 TemplateResolver p_resolver,
                                 TemplateDescriber p_describer,
                                 ImplAnalyzer p_analyzer)
        throws IOException
    {
        String fragVar = p_analyzer.newVarName();

        String fargName = (String) getFargName(p_fargName,
                                               p_analyzer,
                                               p_describer);
        FargInfo fargInfo =
            isDefCall(p_analyzer)
            ? p_analyzer.getFargInfo(fargName)
            : p_describer.getFargInfo(p_analyzer.getAbsolutePath(getPath()),fargName);

        p_writer.print("    final ");

        String fargIntf = fargInfo.getFargInterfaceName();
        if (! isDefCall(p_analyzer))
        {
            fargIntf =
                p_resolver.getFullyQualifiedIntfClassName
                    (p_analyzer.getAbsolutePath(getPath()))
                + "." + fargIntf;
        }

        String className = fargInfo.getFargInterfaceName()
            + p_analyzer.newVarName();
        p_writer.print  ("class ");
        p_writer.println(className);
        p_writer.println("  extends org.jamon.AbstractTemplateImpl");
        p_writer.print  ("  implements ");
        p_writer.println(fargIntf);
        p_writer.println("{");
        p_writer.print  ("  ");
        p_writer.print  (className);
        p_writer.println("(org.jamon.TemplateManager p_manager) {");
        p_writer.println("    super(p_manager,\"\");");
        p_writer.println("  }");
        p_writer.print("       public void render(");
        for (Iterator a = fargInfo.getArgumentNames(); a.hasNext(); /* */)
        {
            String arg = (String) a.next();
            p_writer.print(fargInfo.getArgumentType(arg));
            p_writer.print(" ");
            p_writer.print(arg);
            if (a.hasNext())
            {
                p_writer.print(", ");
            }
        }
        p_writer.print(") throws ");
        p_writer.print(IOEXCEPTION_CLASS);
        p_writer.println(" {");
        for (Iterator i = p_statements.iterator(); i.hasNext(); /* */)
        {
            ((Statement)i.next()).generateSource(p_writer,
                                                 p_resolver,
                                                 p_describer,
                                                 p_analyzer);
            p_writer.println();
        }
        p_writer.println("    }");
        p_writer.println("  }");

        p_writer.print(className);
        p_writer.print(" ");
        p_writer.print(fragVar);
        p_writer.println(" =");
        p_writer.print("      new ");
        p_writer.print(className);
        p_writer.println(" (this.getTemplateManager());");
        p_writer.print(fragVar);
        p_writer.println(".writeTo(this.getWriter());");
        p_writer.print(fragVar);
        p_writer.println(".escaping(this.getEscaping());");
        m_params.put(fargName, fragVar);
    }

    public void generateSource(PrintWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAnalyzer p_analyzer)
        throws IOException
    {
        if (! m_fragParams.isEmpty())
        {
            p_writer.println("{");
            for (Iterator f = m_fragParams.entrySet().iterator(); f.hasNext(); /* */)
            {
                Map.Entry entry = (Map.Entry) f.next();
                handleFragParam((String) entry.getKey(),
                                (List) entry.getValue(),
                                p_writer,
                                p_resolver,
                                p_describer,
                                p_analyzer);
            }
        }


        if (isDefCall(p_analyzer))
        {
            generateAsDefCall(p_writer, p_analyzer);
        }
        else
        {
            generateAsComponentCall(p_writer,
                                    p_resolver,
                                    p_describer,
                                    p_analyzer,
                                    p_analyzer.getAbsolutePath(m_path));
        }

        if (! m_fragParams.isEmpty())
        {
            p_writer.println("}");
        }
    }

    private void generateAsDefCall(PrintWriter p_writer,
                                   ImplAnalyzer p_analyzer)
        throws IOException
    {
        p_writer.print("__jamon_def__");
        p_writer.print(getPath());
        p_writer.print("(");
        int argNum = 0;
        for (Iterator r = p_analyzer.getRequiredArgNames(getPath()); r.hasNext(); )
        {
            if (argNum++ > 0)
            {
                p_writer.print(",");
            }
            String name = (String) r.next();
            String expr = (String) m_params.get(name);
            if (expr == null)
            {
                throw new JamonException
                    ("No value supplied for required argument " + name);
            }
            p_writer.print("(");
            p_writer.print(expr);
            p_writer.print(")");
        }
        for (Iterator o = p_analyzer.getOptionalArgNames(getPath()); o.hasNext(); )
        {
            if (argNum++ > 0)
            {
                p_writer.print(",");
            }
            String name = (String) o.next();
            p_writer.print("(");
            String expr = (String) m_params.get(name);
            if (expr == null)
            {
                p_writer.print(p_analyzer.getDefault(getPath(),name));
            }
            else
            {
                p_writer.print(expr);
            }
            p_writer.print(")");
        }
        p_writer.println(");");
    }

    private void generateAsComponentCall(PrintWriter p_writer,
                                         TemplateResolver p_resolver,
                                         TemplateDescriber p_describer,
                                         ImplAnalyzer p_analyzer,
                                         String p_absPath)
        throws IOException
    {
        String tVar = p_analyzer.newVarName();
        String intfName = p_resolver.getFullyQualifiedIntfClassName(p_absPath);
        p_writer.println("{");
        p_writer.print("  final ");
        p_writer.print(intfName);
        p_writer.print(" ");
        p_writer.print(tVar);
        p_writer.print(" = new ");
        p_writer.print(intfName);
        p_writer.println("(this.getTemplateManager());");
        p_writer.print("  ");
        p_writer.print(tVar);
        p_writer.println(".writeTo(this.getWriter());");
        p_writer.print(tVar);
        p_writer.println(".escaping(this.getEscaping());");

        List requiredArgs = p_describer.getRequiredArgNames(p_absPath);

        for (Iterator i = m_params.keySet().iterator(); i.hasNext(); /* */ )
        {
            String name = (String) i.next();
            if (! requiredArgs.contains(name) )
            {
                p_writer.print("      ");
                p_writer.print(tVar);
                p_writer.print(".set");
                p_writer.print(StringUtils.capitalize(name));
                p_writer.print("(");
                p_writer.print(m_params.get(name));
                p_writer.println(");");
            }
        }
        p_writer.print("      ");
        p_writer.print(tVar);
        p_writer.print(".render(");
        for (Iterator i = requiredArgs.iterator(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            String expr = (String) m_params.get(name);
            if (expr == null)
            {
                throw new JamonException("No parameter supplied for argument "
                                         + name
                                         + " in call to "
                                         + getPath());
            }
            p_writer.print(expr);
            if (i.hasNext())
            {
                p_writer.print(",");
            }
        }
        p_writer.println(");");
        p_writer.print("    }");
    }

    private final String getPath()
    {
        return m_path;
    }
}
