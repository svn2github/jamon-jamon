package org.modusponens.jtt;

import java.io.PrintWriter;
import java.io.IOException;

public class RawStatement
    implements Statement
{
    RawStatement(String p_code)
    {
        m_code = p_code;
    }

    public void generateSource(PrintWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAnalyzer p_analyzer)
        throws IOException
    {
        p_writer.println(m_code);
    }

    private final String m_code;
}
