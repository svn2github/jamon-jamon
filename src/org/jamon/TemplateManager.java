package org.modusponens.jtt;

import java.io.Writer;

public interface TemplateManager
{
    public Template getInstance(String p_path, Writer p_writer)
        throws JttException;
}
