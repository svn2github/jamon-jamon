package org.modusponens.jtt;

import java.io.Writer;
import java.io.IOException;
import java.net.URLEncoder;

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
        // FIXME: need to set default escaping
        writeHtmlEscaped(p_obj);
    }

    protected void write(long p_long)
        throws IOException
    {
        write(new Long(p_long));
    }

    protected void writeHtmlEscaped(long p_long)
        throws IOException
    {
        writeHtmlEscaped(new Long(p_long));
    }

    protected void writeUrlEscaped(long p_long)
        throws IOException
    {
        writeUrlEscaped(new Long(p_long));
    }

    protected void writeXmlEscaped(long p_long)
        throws IOException
    {
        writeXmlEscaped(new Long(p_long));
    }

    protected void writeHtmlEscaped(Object p_obj)
        throws IOException
    {
        if (p_obj != null)
        {
            writeHtmlEscaped(p_obj.toString());
        }
    }

    protected void writeXmlEscaped(Object p_obj)
        throws IOException
    {
        if (p_obj != null)
        {
            writeXmlEscaped(p_obj.toString());
        }
    }

    protected void writeUrlEscaped(Object p_obj)
        throws IOException
    {
        if (p_obj != null)
        {
            writeUrlEscaped(p_obj.toString());
        }
    }

    protected void writeUnEscaped(Object p_obj)
        throws IOException
    {
        if (p_obj != null)
        {
            m_writer.write(p_obj.toString());
        }
    }

    protected TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }

    private void writeHtmlEscaped(String p_string)
        throws IOException
    {
        for (int i = 0;i < p_string.length(); ++i)
        {
            char c = p_string.charAt(i);
            switch (c)
            {
              case '<': m_writer.write("&lt;"); break;
              case '>': m_writer.write("&gt;"); break;
              case '&': m_writer.write("&amp;"); break;
                // FIXME: numerically escape other chars
              default: m_writer.write(c);
            }
        }
    }

    private void writeXmlEscaped(String p_string)
        throws IOException
    {
        for (int i = 0;i < p_string.length(); ++i)
        {
            char c = p_string.charAt(i);
            switch (c)
            {
              case '<': m_writer.write("&lt;"); break;
              case '>': m_writer.write("&gt;"); break;
              case '&': m_writer.write("&amp;"); break;
              case '"': m_writer.write("&quot;"); break;
              case '\'': m_writer.write("&apos;"); break;
                // FIXME: numerically escape other chars
              default: m_writer.write(c);
            }
        }
    }

    private void writeUrlEscaped(String p_string)
        throws IOException
    {
        m_writer.write(URLEncoder.encode(p_string));
    }

    private final Writer m_writer;
    private final TemplateManager m_templateManager;
}
