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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jamon.FileTemplateSource;
import org.jamon.codegen.FragmentArgument;
import org.jamon.codegen.MethodUnit;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.TemplateDescription;

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
                                  getClass().getClassLoader());
    }

    public void testJamonContext() throws Exception {
        TemplateDescription desc =
            m_describer.getTemplateDescription("/test/jamon/context/ContextCallee",
                                               null);
        assertEquals("org.jamon.integration.TestJamonContext", desc.getJamonContextType());
    }

    public void testNoJamonContext() throws Exception {
        TemplateDescription desc =
            m_describer.getTemplateDescription("/test/jamon/ClassOnly",
                                               null);
        assertNull("", desc.getJamonContextType());
    }

    public void testArgumentIntrospection()
        throws Exception
    {
        TemplateDescription desc =
            m_describer.getTemplateDescription("/test/jamon/ClassOnly",
                                               null);
        NameType.checkArgs(
            desc.getRequiredArgs(),
            new NameType("i", "int"), new NameType("j", "Integer"));
        NameType.checkArgs(desc.getOptionalArgs(), new NameType("foo", "String"));
    }


    public void testFragmentUnitIntrospection()
        throws Exception
    {
        List<FragmentArgument> fragmentUnitIntfs =
            m_describer.getTemplateDescription(
                "/test/jamon/ClassOnly", null)
            .getFragmentInterfaces();

        assertEquals(2, fragmentUnitIntfs.size());
        FragmentArgument f2 = fragmentUnitIntfs.get(0);
        FragmentArgument f1 = fragmentUnitIntfs.get(1);

        assertEquals("f1", f1.getName());
        assertEquals("f2", f2.getName());
        NameType[] p_nameTypes = { new NameType("k", "int"), new NameType("m", "Boolean[]"), new NameType("a1", "String"), new NameType("a4", "String"), new NameType("a2", "String"), new NameType("a3", "String"), new NameType("a5", "String") };
        NameType.checkArgs(f1.getFragmentUnit().getRequiredArgs(), p_nameTypes);
        NameType[] p_nameTypes1 = {};

        NameType.checkArgs(f2.getFragmentUnit().getRequiredArgs(), p_nameTypes1);
    }

    public void testMethodUnitIntrospection()
        throws Exception
    {
        Map<String, MethodUnit> methods = m_describer
            .getTemplateDescription("/test/jamon/ClassOnly", null)
            .getMethodUnits();
        assertEquals(1, methods.size());
        MethodUnit method = methods.get("m");

        assertNotNull(method);
        assertEquals("m", method.getName());
        NameType.checkArgs(method.getSignatureRequiredArgs(), new NameType("mi", "int"));
        NameType.checkArgs(method.getSignatureOptionalArgs(), new NameType("mj", "int"));

        Collection<FragmentArgument> fragments = method.getFragmentArgs();
        assertEquals(1, fragments.size());
        FragmentArgument frag = fragments.iterator().next();
        assertEquals("mf", frag.getName());
        NameType[] p_nameTypes = { new NameType("mk", "int") };
        NameType.checkArgs(frag.getFragmentUnit().getRequiredArgs(), p_nameTypes);
    }

    public void checkGenericIntrospection() throws Exception
    {
        assertEquals(
            3,
            m_describer.getTemplateDescription(
                "test/jamon/ClassOnly", null).getGenericParamsCount());
    }

}
