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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2002 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s): Jay Sachs
 */

package org.jamon.integration;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.jamon.codegen.Argument;
import org.jamon.codegen.FargInfo;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.FileTemplateSource;

/**
 * Test Jamon's java escapes.  See "Jamon User's Guide", section 2.
 **/

public class TemplateDescriberTest
    extends TestCase
{
    private TemplateDescriber m_describer;

    public void setUp()
        throws Exception
    {
        File nonexistent = File.createTempFile("jamontest",null);
        nonexistent.deleteOnExit();
        m_describer =
            new TemplateDescriber(new FileTemplateSource(nonexistent));
    }

    public void testArgumentIntrospection()
        throws Exception
    {
        List argNames =
            m_describer.getRequiredArgNames("/test/jamon/ClassOnly");
        assertEquals(2, argNames.size());
        assertEquals("i", argNames.get(0));
        assertEquals("j", argNames.get(1));
    }


    public void testArgumentWithFargIntrospection()
        throws Exception
    {
        List argNames =
            m_describer.getRequiredArgNames("/test/jamon/ClassOnly2");
        assertEquals(4, argNames.size());
        assertEquals("i", argNames.get(0));
        assertEquals("j", argNames.get(1));
        assertEquals("f2", argNames.get(2));
        assertEquals("f1", argNames.get(3));
    }

    public void testFargNameIntrospection()
        throws Exception
    {
        LinkedList fargNames = new LinkedList();
        for (Iterator f = m_describer.getFargNames("/test/jamon/ClassOnly2");
            f.hasNext(); )
        {
            fargNames.add(f.next());
        }
        assertEquals(2, fargNames.size());
        assertEquals("f2", fargNames.get(0));
        assertEquals("f1", fargNames.get(1));
    }


    public void testFargInfoIntrospection()
        throws Exception
    {
        FargInfo info = m_describer.getFargInfo("/test/jamon/ClassOnly2","f1");
        assertEquals("f1", info.getName());
        assertTrue(info.hasRequiredArgs());
        Iterator i = info.getRequiredArgs();
        Argument a = (Argument) i.next();
        assertEquals("k",a.getName());
        assertEquals("int", a.getType());
        a = (Argument) i.next();
        assertEquals("m",a.getName());
        assertEquals("Boolean[]", a.getType());

        info = m_describer.getFargInfo("/test/jamon/ClassOnly2","f2");
        assertEquals("f2", info.getName());
        assertTrue(! info.hasRequiredArgs());
        i = info.getRequiredArgs();
        assertTrue(! i.hasNext());
    }

}
