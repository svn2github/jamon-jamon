package org.jamon.integration;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import gnu.regexp.RE;

import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManager;

public class TestBase
    extends TestCase
{
    private TemplateManager m_templateManager;
    private StringWriter m_writer;

    public void setUp()
        throws Exception
    {
        m_templateManager = new StandardTemplateManager()
            .setSourceDir("templates")
            .setWorkDir("build/work");
        m_writer = new StringWriter();
    }

    protected Writer getWriter()
        throws IOException
    {
        return m_writer;
    }

    private String getOutput()
    {
        m_writer.flush();
        return m_writer.toString();
    }

    protected void checkOutput(String p_expected)
        throws IOException
    {
        assertEquals(p_expected, getOutput());
    }

    protected void checkOutput(RE p_regexp)
        throws Exception
    {
        assertTrue(p_regexp.isMatch(getOutput()));
    }

    protected TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }
}
