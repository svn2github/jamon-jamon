package org.modusponens.jtt;

import java.io.Writer;
import java.io.IOException;

public abstract class AbstractTemplate
    implements Template
{
    protected AbstractTemplate(Writer p_writer,
                               TemplateManager p_templateManager)
    {
        m_writer = p_writer;
        m_templateManager = p_templateManager;
    }

    protected void write(Object p_obj)
        throws IOException
    {
        // FIXME: test for null. what to do?
        m_writer.write(p_obj.toString());
    }

    protected void write(int p_int)
        throws IOException
    {
        write(new Integer(p_int));
    }

    protected void writeHtmlEscaped(String p_string)
        throws IOException
    {
        // FIXME
        m_writer.write(p_string);
    }

    protected void writeXmlEscaped(String p_string)
        throws IOException
    {
        // FIXME
        m_writer.write(p_string);
    }

    protected void writeUrlEscaped(String p_string)
        throws IOException
    {
        // FIXME
        m_writer.write(p_string);
    }

    protected TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }

    private final Writer m_writer;
    private final TemplateManager m_templateManager;
}
