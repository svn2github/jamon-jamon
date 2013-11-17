/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.templates;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.jamon.BasicTemplateManager;
import org.jamon.TemplateManager;

public abstract class TestBase
    extends TestCase
{

    @Override public void setUp()
        throws Exception
    {
        m_templateManager = new BasicTemplateManager();
        m_writer = new StringWriter();
    }

    protected Writer getWriter()
        throws IOException
    {
        return m_writer;
    }

    protected void checkOutputContains(String p_expected)
        throws IOException
    {
        assertTrue(getOutput().indexOf(p_expected) >= 0);
    }

    protected void checkOutput(String p_expected)
        throws IOException
    {
        assertEquals(p_expected, getOutput());
    }

    protected void checkOutput(String p_message, String p_expected)
        throws IOException
    {
        assertEquals(p_message, p_expected, getOutput());
    }

    protected TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }

    private String getOutput()
    {
        m_writer.flush();
        return m_writer.toString();
    }

    private TemplateManager m_templateManager;
    private StringWriter m_writer;

}
