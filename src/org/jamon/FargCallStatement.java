package org.jamon;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

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

    public void generateSource(PrintWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAnalyzer p_analyzer)
        throws IOException
    {
        // FIXME!
        p_writer.print  (getPath());
        p_writer.print  (".render(");
        int argNum = 0;
        for (Iterator r = m_fargInfo.getArgumentNames(); r.hasNext(); /* */)
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
        p_writer.println(");");
    }

    private String getPath()
    {
        return m_path;
    }
}
