/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.templates;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.jamon.BasicTemplateManager;
import org.jamon.RecompilingTemplateManager;
import org.jamon.TemplateManager;

public class TestBase
    extends TestCase
{

    public void setUp()
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
