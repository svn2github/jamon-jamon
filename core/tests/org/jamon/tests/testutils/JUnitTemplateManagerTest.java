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
    public void testSuccess1()
        throws Exception
    {
        JUnitTemplateManager manager =
            new JUnitTemplateManager(FakeTemplate.class,
                                     new HashMap(),
                                     new Object[] { Boolean.TRUE, "hello" });
        FakeTemplate template = new FakeTemplate(manager);
        StringWriter writer = new StringWriter();
        template
            .writeTo(writer)
            .autoFlush(false)
            .escaping(Escaping.NONE)
            .render(true,"hello");
        writer.flush();
        assertEquals("", writer.toString());
    }

    public void testSuccess2()
        throws Exception
    {
        HashMap optMap = new HashMap();
        optMap.put("i", new Integer(4));
        JUnitTemplateManager manager =
            new JUnitTemplateManager(FakeTemplate.class,
                                     optMap,
                                     new Object[] { Boolean.TRUE, "hello" });
        FakeTemplate template = new FakeTemplate(manager);
        StringWriter writer = new StringWriter();
        template
            .writeTo(writer)
            .autoFlush(false)
            .escaping(Escaping.NONE)
            .setI(4)
            .render(true,"hello");
        writer.flush();
        assertTrue(manager.getWasRendered());
        assertEquals("", writer.toString());
    }


    public void testMissingOptionalArg()
        throws Exception
    {
        HashMap optMap = new HashMap();
        optMap.put("i", new Integer(4));
        JUnitTemplateManager manager =
            new JUnitTemplateManager(FakeTemplate.class,
                                     optMap,
                                     new Object[] { Boolean.TRUE, "hello" });
        FakeTemplate template = new FakeTemplate(manager);
        StringWriter writer = new StringWriter();
        template
            .writeTo(writer)
            .autoFlush(false)
            .escaping(Escaping.NONE);
        try
        {
            template.render(true,"hello");
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
        HashMap optMap = new HashMap();
        JUnitTemplateManager manager =
            new JUnitTemplateManager(FakeTemplate.class,
                                     optMap,
                                     new Object[] { Boolean.TRUE, "hello" });
        FakeTemplate template = new FakeTemplate(manager);
        StringWriter writer = new StringWriter();
        template
            .writeTo(writer)
            .autoFlush(false)
            .escaping(Escaping.NONE);
        try
        {
            template.setI(3);
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
        HashMap optMap = new HashMap();
        JUnitTemplateManager manager =
            new JUnitTemplateManager(FakeTemplate.class,
                                     optMap,
                                     new Object[] { Boolean.FALSE, "hello" });
        FakeTemplate template = new FakeTemplate(manager);
        StringWriter writer = new StringWriter();
        template
            .writeTo(writer)
            .autoFlush(false)
            .escaping(Escaping.NONE);
        try
        {
            template.render(true, "hello");
        }
        catch( AssertionFailedError e )
        {
            assertEquals("render argument[0] expected false, got true",
                         e.getMessage());
        }
    }


    public void testMismatchOptionalArg()
        throws Exception
    {
        HashMap optMap = new HashMap();
        optMap.put("i", new Integer(4));
        JUnitTemplateManager manager =
            new JUnitTemplateManager(FakeTemplate.class,
                                     optMap,
                                     new Object[] { Boolean.FALSE, "hello" });
        FakeTemplate template = new FakeTemplate(manager);
        StringWriter writer = new StringWriter();
        template
            .writeTo(writer)
            .autoFlush(false)
            .escaping(Escaping.NONE);
        try
        {
            template.setI(3);
        }
        catch( AssertionFailedError e )
        {
            assertEquals("setI argument[0] expected 4, got 3",
                         e.getMessage());
        }
    }

}
