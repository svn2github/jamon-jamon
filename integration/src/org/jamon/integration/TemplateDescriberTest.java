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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
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

import org.jamon.FileTemplateSource;

import org.jamon.codegen.FragmentArgument;
import org.jamon.codegen.FragmentUnit;
import org.jamon.codegen.RequiredArgument;
import org.jamon.codegen.TemplateDescriber;

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
        List argNames = m_describer
            .getTemplateDescription("/test/jamon/ClassOnly")
            .getRequiredArgs() ;
        assertEquals(2, argNames.size());
        assertEquals("i", ((RequiredArgument) argNames.get(0)).getName());
        assertEquals("j", ((RequiredArgument) argNames.get(1)).getName());
    }


    public void testArgumentWithFargIntrospection()
        throws Exception
    {
        List argNames = m_describer
            .getTemplateDescription("/test/jamon/ClassOnly2")
            .getRequiredArgs() ;
        assertEquals(2, argNames.size());
        assertEquals("i", ((RequiredArgument) argNames.get(0)).getName());
        assertEquals("j", ((RequiredArgument) argNames.get(1)).getName());
    }

    public void testFragmentUnitIntrospection()
        throws Exception
    {
        List fragmentUnitIntfs =
            m_describer.getTemplateDescription("/test/jamon/ClassOnly2")
            .getFragmentInterfaces();

        assertEquals(2, fragmentUnitIntfs.size());
        FragmentArgument f2 = (FragmentArgument) fragmentUnitIntfs.get(0);
        FragmentArgument f1 = (FragmentArgument) fragmentUnitIntfs.get(1);

        assertEquals("f1", f1.getName());
        assertTrue(f1.getFragmentUnit().hasRequiredArgs());
        Iterator f1Args = f1.getFragmentUnit().getRequiredArgs();
        checkArgument(f1Args,"k","int");
        checkArgument(f1Args,"m","Boolean[]");
        checkArgument(f1Args,"a1","String");
        checkArgument(f1Args,"a4","String");
        checkArgument(f1Args,"a2","String");
        checkArgument(f1Args,"a3","String");
        checkArgument(f1Args,"a5","String");
        assertTrue(! f1Args.hasNext());

        assertEquals("f2", f2.getName());
        assertTrue(! f2.getFragmentUnit().hasRequiredArgs());
    }

    private void checkArgument(Iterator p_argIter,
                               String p_name,
                               String p_type)
    {
        RequiredArgument a = (RequiredArgument) p_argIter.next();
        assertEquals(p_name,a.getName());
        assertEquals(p_type, a.getType());
    }

}
