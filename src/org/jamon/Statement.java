package org.modusponens.jtt;

import java.io.PrintWriter;
import java.io.IOException;

public interface Statement
{
    void generateSource(PrintWriter p_writer,
                        TemplateResolver p_resolver,
                        TemplateDescriber p_describer,
                        ImplAnalyzer p_analyzer)
        throws IOException;
}
