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
 * Contributor(s):
 */

package org.jamon.tests.codegen;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jamon.codegen.FragmentArgument;
import org.jamon.codegen.FragmentUnit;
import org.jamon.codegen.AbstractArgument;
import org.jamon.codegen.OptionalArgument;
import org.jamon.codegen.RequiredArgument;
import org.jamon.codegen.TemplateUnit;
import org.jamon.codegen.TemplateDescription;
import org.jamon.codegen.TunnelingException;

public class TemplateUnitTest
    extends TestCase
{

    public void testInheritanceDepth() throws Exception
    {
        TemplateUnit parent = new TemplateUnit("/parent");
        TemplateUnit child = new TemplateUnit("/child");
        TemplateUnit grandchild = new TemplateUnit("/grandchild");
        child.setParentDescription(new TemplateDescription(parent));
        grandchild.setParentDescription(new TemplateDescription(child));

        assertEquals(0, parent.getInheritanceDepth());
        assertEquals(1, child.getInheritanceDepth());
        assertEquals(2, grandchild.getInheritanceDepth());
    }

    public void testParentArgs() throws Exception
    {
        TemplateUnit parent = new TemplateUnit("/parent");
        TemplateUnit child = new TemplateUnit("/child");
        TemplateUnit grandchild = new TemplateUnit("/grandchild");

        RequiredArgument pr1 = new RequiredArgument("pr1", "int");
        RequiredArgument pr2 = new RequiredArgument("pr2", "int");
        RequiredArgument cr3 = new RequiredArgument("cr2", "int");
        OptionalArgument po1 = new OptionalArgument("po1", "int", "op1");
        OptionalArgument po2 = new OptionalArgument("po2", "int", "op2");
        OptionalArgument co3 = new OptionalArgument("co2", "int", "oc3");

        parent.addRequiredArg(pr1);
        parent.addRequiredArg(pr2);
        parent.addOptionalArg(po1);
        parent.addOptionalArg(po2);
        child.setParentPath(parent.getName());
        child.setParentDescription(new TemplateDescription(parent));

        child.addParentArg("pr2", null);
        child.addParentArg("po2", "oc2");
        child.addRequiredArg(cr3);
        child.addOptionalArg(co3);

        checkArgList(new RequiredArgument[] {pr1, pr2, cr3},
                     child.getSignatureRequiredArgs());
        checkArgList(new RequiredArgument[] {cr3},
                     child.getDeclaredRenderArgs());
        checkArgSet(new AbstractArgument[] {pr2, cr3, po2, co3},
                     child.getVisibleArgs());
        checkArgSet(new OptionalArgument[] {po1, po2, co3},
                     child.getSignatureOptionalArgs());
        checkArgSet(new OptionalArgument[] {co3},
                     child.getDeclaredOptionalArgs());

        FragmentArgument f =
            new FragmentArgument( new FragmentUnit("f", child));
        child.addFragmentArg(f);
        checkArgSet(new AbstractArgument[] {pr2, cr3, po2, co3, f},
                    child.getVisibleArgs());

        grandchild.setParentDescription(new TemplateDescription(child));
        checkArgList(new RequiredArgument[] {pr1, pr2, cr3},
                     grandchild.getSignatureRequiredArgs());
        checkArgList(new RequiredArgument[0],
                     grandchild.getDeclaredRenderArgs());
        checkArgSet(new AbstractArgument[] {}, grandchild.getVisibleArgs());
        checkArgSet(new OptionalArgument[] {po1, po2, co3},
                    grandchild.getSignatureOptionalArgs());
        checkArgSet(new OptionalArgument[0],
                    grandchild.getDeclaredOptionalArgs());
    }

    public void testSignature()
        throws Exception
    {
        //FIXME - test imports
        TemplateUnit unit = new TemplateUnit("/foo");
        TemplateUnit parent = new TemplateUnit("/bar");

        Set sigs = new HashSet();
        checkSigIsUnique(unit, sigs);

        RequiredArgument i = new RequiredArgument("i", "int");
        RequiredArgument j = new RequiredArgument("j", "Integer");
        OptionalArgument a = new OptionalArgument("a", "boolean", "true");
        OptionalArgument b = new OptionalArgument("b", "Boolean", "null");
        FragmentUnit f = new FragmentUnit("f", null);
        FragmentUnit g = new FragmentUnit("g", null);

        unit.addRequiredArg(i);
        checkSigIsUnique(unit, sigs);

        unit.addRequiredArg(j);
        checkSigIsUnique(unit, sigs);

        unit.addOptionalArg(a);
        checkSigIsUnique(unit, sigs);

        unit.addOptionalArg(b);
        checkSigIsUnique(unit, sigs);

        unit = new TemplateUnit("/foo");
        unit.setParentDescription(new TemplateDescription(parent));
        checkSigIsUnique(unit, sigs);

        unit = new TemplateUnit("/foo");
        parent.addRequiredArg(i);
        unit.setParentDescription(new TemplateDescription(parent));
        // suboptimal - if the parent's sig changes, so does the child's
        checkSigIsUnique(unit, sigs);

        unit.addFragmentArg(new FragmentArgument(f));
        checkSigIsUnique(unit, sigs);
        f.addRequiredArg(new RequiredArgument("x", "float"));
        checkSigIsUnique(unit, sigs);
        unit.addFragmentArg(new FragmentArgument(g));
        checkSigIsUnique(unit, sigs);

        unit.addImport("java.util.Date");
        checkSigIsUnique(unit, sigs);
    }


    public void testDependencies()
        throws Exception
    {
        TemplateUnit unit = new TemplateUnit("/foo/bar");
        unit.addCallPath("/baz");
        unit.addCallPath("/foo/wazza");
        unit.setParentPath("/foo/balla");
        Collection dependencies = unit.getTemplateDependencies();
        assertEquals(3, dependencies.size());
        assertTrue(dependencies.contains("/baz"));
        assertTrue(dependencies.contains("/foo/balla"));
        assertTrue(dependencies.contains("/foo/wazza"));
    }

    private void checkSigIsUnique(TemplateUnit p_unit, Set p_set)
        throws Exception
    {
        String sig = p_unit.getSignature();
        assertTrue(! p_set.contains(sig));
        p_set.add(sig);
    }

    private void checkArgList(AbstractArgument[] p_expected,
                              Iterator p_actual)
    {
        for(int i = 0; i < p_expected.length; i++)
        {
            assertEquals(p_expected[i].getName(),
                         ((AbstractArgument) p_actual.next()).getName());
        }
        assertTrue(! p_actual.hasNext());
    }

    private void checkArgSet(AbstractArgument[] p_expected,
                                          Iterator p_actual)
    {
        Map actual = new HashMap();
        while (p_actual.hasNext())
        {
            AbstractArgument arg = (AbstractArgument) p_actual.next();
            actual.put(arg.getName(), arg);
        }
        assertEquals(p_expected.length, actual.size());
        for (int i = 0; i < p_expected.length; i++)
        {
            assertTrue(actual.remove(p_expected[i].getName()) != null);
        }
    }
}
