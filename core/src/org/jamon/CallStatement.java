package org.modusponens.jtt;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.modusponens.jtt.node.AParam;

public class CallStatement
    implements Statement
{
    CallStatement(String p_path,List p_params)
    {
        m_path = p_path;
        m_params = new HashMap();
        for (Iterator p = p_params.iterator(); p.hasNext(); /* */)
        {
            AParam param = (AParam) p.next();
            m_params.put(param.getIdentifier().getText(),
                         param.getParamExpr().getText().trim());
        }
    }
    private final String m_path;
    protected final Map m_params;

    protected boolean isDefCall(ImplAdapter p_adapter)
    {
        return p_adapter.getDefNames().contains(getPath());
    }

    public void generateSource(PrintWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAdapter p_adapter)
        throws IOException
    {
        if (isDefCall(p_adapter))
        {
            generateAsDefCall(p_writer, p_adapter);
        }
        else
        {
            generateAsComponentCall(p_writer,
                                    p_resolver,
                                    p_describer,
                                    p_adapter,
                                    p_adapter.getAbsolutePath(m_path));
        }
    }

    private void generateAsDefCall(PrintWriter p_writer,
                                   ImplAdapter p_adapter)
        throws IOException
    {
        p_writer.print("$def$");
        p_writer.print(getPath());
        p_writer.print("(");
        int argNum = 0;
        for (Iterator r = p_adapter.getRequiredArgNames(getPath()); r.hasNext(); )
        {
            if (argNum++ > 0)
            {
                p_writer.print(",");
            }
            String name = (String) r.next();
            String expr = (String) m_params.get(name);
            if (expr == null)
            {
                throw new JttException("No value supplied for required argument "
                                       + name);
            }
            p_writer.print("(");
            p_writer.print(expr);
            p_writer.print(")");
        }
        for (Iterator o = p_adapter.getOptionalArgNames(getPath()); o.hasNext(); )
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
                p_writer.print(p_adapter.getDefault(getPath(),name));
            }
            else
            {
                p_writer.print(expr);
            }
            p_writer.print(")");
        }
        p_writer.println(");");
    }

    private String getInterfaceClassName(TemplateResolver p_resolver,
                                         String p_absPath)
    {
        String pkg =
            p_resolver.getIntfPackageName(p_absPath);
        String clsName =
            p_resolver.getIntfClassName(p_absPath);
        return "".equals(pkg) ? clsName : (pkg + "." + clsName);
    }

    private void generateAsComponentCall(PrintWriter p_writer,
                                         TemplateResolver p_resolver,
                                         TemplateDescriber p_describer,
                                         ImplAdapter p_adapter,
                                         String p_absPath)
        throws IOException
    {
        String tVar = p_adapter.newVarName();
        String intfName = getInterfaceClassName(p_resolver,p_absPath);
        p_writer.println("{");
        p_writer.print("  final ");
        p_writer.print(intfName);
        p_writer.print(" ");
        p_writer.print(tVar);
        p_writer.print(" = (");
        p_writer.print(intfName);
        p_writer.print(") getTemplateManager().getInstance(\"");
        p_writer.print(p_absPath);
        p_writer.println("\", getWriter());");

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
                throw new JttException("No parameter supplied for argument "
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

    protected final String getPath()
    {
        return m_path;
    }

    private String getAbsolutePath(String p_callingPath)
    {
        // FIXME: should use properties ...
        if (getPath().charAt(0) == '/')
        {
            return getPath();
        }
        else
        {
            int i = p_callingPath.lastIndexOf('/');
            if (i <= 0)
            {
                return "/" + getPath();
            }
            else
            {
                return p_callingPath.substring(0,i) + "/" + getPath();
            }
        }

    }

}
