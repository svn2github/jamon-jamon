package org.modusponens.jtt;

import java.io.PrintWriter;
import java.io.IOException;

public class WriteStatement
    implements Statement
{
    WriteStatement(String p_expr, Encoding p_encoding)
    {
        m_expr = p_expr;
        m_encoding = p_encoding;
    }

    public void generateSource(PrintWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAnalyzer p_analyzer)
        throws IOException
    {
        p_writer.println("write"
                         + m_encoding
                         + "Escaped(String.valueOf("
                         + m_expr
                         + "));");
    }

    private final String m_expr;
    private final Encoding m_encoding;
}
