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
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jamon.FileTemplateSource;

import org.jamon.codegen.FragmentArgument;
import org.jamon.codegen.MethodUnit;
import org.jamon.codegen.AbstractArgument;
import org.jamon.codegen.RequiredArgument;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.TemplateDescription;
import org.jamon.emit.EmitMode;

public class TemplateDescriberTest
    extends TestCase
{
    private TemplateDescriber m_describer;

    @Override public void setUp()
        throws Exception
    {
        File nonexistent = File.createTempFile("jamontest",null);
        nonexistent.deleteOnExit();
        m_describer =
            new TemplateDescriber(new FileTemplateSource(nonexistent),
                                  getClass().getClassLoader(),
                                  EmitMode.STANDARD);
    }

    public void testArgumentIntrospection()
        throws Exception
    {
        TemplateDescription desc =
            m_describer.getTemplateDescription("/test/jamon/ClassOnly",
                                               null,
                                               null);
        checkArgs(desc.getRequiredArgs().iterator(),
                  new String[] {"i", "j"},
                  new String[] {"int", "Integer"});
        checkArgs(desc.getOptionalArgs().iterator(),
                  new String[] {"foo"},
                  new String[] {"String"});
    }


    public void testFragmentUnitIntrospection()
        throws Exception
    {
        List fragmentUnitIntfs =
            m_describer.getTemplateDescription(
                "/test/jamon/ClassOnly", null, null)
            .getFragmentInterfaces();

        assertEquals(2, fragmentUnitIntfs.size());
        FragmentArgument f2 = (FragmentArgument) fragmentUnitIntfs.get(0);
        FragmentArgument f1 = (FragmentArgument) fragmentUnitIntfs.get(1);

        assertEquals("f1", f1.getName());
        assertEquals("f2", f2.getName());
        checkFragArgArgs
            (f1, new String[] {"k", "m", "a1", "a4", "a2", "a3", "a5"});
        checkFragArgArgs(f2, new String[0]);
    }

    public void testMethodUnitIntrospection()
        throws Exception
    {
        Map methods = m_describer
            .getTemplateDescription("/test/jamon/ClassOnly", null, null)
            .getMethodUnits();
        assertEquals(1, methods.size());
        MethodUnit method = (MethodUnit) methods.get("m");

        assertNotNull(method);
        assertEquals("m", method.getName());
        checkArgs(method.getSignatureRequiredArgs(),
                  new String[] {"mi"},
                  new String[] {"int"});
        checkArgs(method.getSignatureOptionalArgs(),
                  new String[] {"mj"},
                  new String[] {"int"});

        Iterator fragments = method.getFragmentArgs();
        assertTrue(fragments.hasNext());
        FragmentArgument frag = (FragmentArgument) fragments.next();
        assertTrue(! fragments.hasNext());
        assertEquals("mf", frag.getName());
        checkFragArgArgs(frag, new String[] {"mk"});
    }

    public void checkGenericIntrospection() throws Exception
    {
        assertEquals(
            3,
            m_describer.getTemplateDescription(
                "test/jamon/ClassOnly", null, null).getGenericParamsCount());
    }

    private void checkArgs(Iterator p_args, String[] p_names, String[] p_types)
    {
        assertEquals(p_names.length, p_types.length);
        for (int i = 0; i < p_names.length; i++)
        {
            assertTrue(p_args.hasNext());
            AbstractArgument a = (AbstractArgument) p_args.next();
            assertEquals(p_names[i], a.getName());
            assertEquals(p_types[i], a.getType());
        }
        assertTrue(! p_args.hasNext());
    }

    private void checkFragArgArgs(FragmentArgument f, String[] p_names)
    {
        Iterator args = f.getFragmentUnit().getRequiredArgs();
        for (int i = 0; i < p_names.length; i++)
        {
            assertTrue(args.hasNext());
            RequiredArgument a = (RequiredArgument) args.next();
            assertEquals(p_names[i], a.getName());
            assertNull(a.getType());
        }
        assertTrue(! args.hasNext());
    }

}
