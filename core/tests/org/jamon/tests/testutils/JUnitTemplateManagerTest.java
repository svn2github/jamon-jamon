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

package org.jamon.tests.testutils;

import java.io.StringWriter;
import java.util.HashMap;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

import org.jamon.testutils.JUnitTemplateManager;
import org.jamon.escaping.Escaping;

public class JUnitTemplateManagerTest
    extends TestCase
{
    private FakeTemplate m_template;
    private JUnitTemplateManager m_manager;
    private StringWriter m_writer;

    private void prepareTemplate(Integer p_iValue)
        throws Exception
    {
        HashMap optMap = new HashMap();
        if (p_iValue != null)
        {
            optMap.put("i", p_iValue);
        }
        m_manager =
            new JUnitTemplateManager(FakeTemplate.class,
                                     optMap,
                                     new Object[] { Boolean.TRUE, "hello" });
        m_template = new FakeTemplate(m_manager);
        m_writer = new StringWriter();
        m_template
            .writeTo(m_writer)
            .autoFlush(false)
            .escaping(Escaping.NONE);
    }

    private void checkSuccess()
        throws Exception
    {
        m_writer.flush();
        assertTrue(m_manager.getWasRendered());
        assertEquals("", m_writer.toString());
    }

    public void testSuccess1()
        throws Exception
    {
        prepareTemplate(null);
        m_template.render(true,"hello");
        checkSuccess();
    }

    public void testSuccess2()
        throws Exception
    {
        prepareTemplate(new Integer(4));
        m_template
            .setI(4)
            .render(true,"hello");
        checkSuccess();
    }

    public void testMissingOptionalArg()
        throws Exception
    {
        prepareTemplate(new Integer(4));
        try
        {
            m_template.render(true,"hello");
        }
        catch( AssertionFailedError e )
        {
            assertEquals("all optional arguments not set before render",
                         e.getMessage());
        }
    }

    public void testUnexpectedOptionalArg()
        throws Exception
    {
        prepareTemplate(null);
        try
        {
            m_template.setI(3);
        }
        catch( AssertionFailedError e )
        {
            assertEquals("unexpected optional argument i",
                         e.getMessage());
        }
    }

    public void testMismatchRequiredArg()
        throws Exception
    {
        prepareTemplate(null);
        try
        {
            m_template.render(false,"hello");
        }
        catch( AssertionFailedError e )
        {
            assertEquals("render argument[0] expected true, got false",
                         e.getMessage());
        }
    }


    public void testMismatchOptionalArg()
        throws Exception
    {
        prepareTemplate(new Integer(4));
        try
        {
            m_template.setI(3);
        }
        catch( AssertionFailedError e )
        {
            assertEquals("setI argument[0] expected 4, got 3",
                         e.getMessage());
        }
    }

}
