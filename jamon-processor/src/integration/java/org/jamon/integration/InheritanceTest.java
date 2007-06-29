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
 * Contributor(s): Ian Robertson
 */

package org.jamon.integration;

import java.lang.reflect.Modifier;

import org.jamon.annotations.Template;
import org.jamon.compiler.TemplateCompilationException;

import test.jamon.Child;
import test.jamon.Child1;
import test.jamon.Child2;
import test.jamon.ChildFragmentCaller;
import test.jamon.Grandchild;
import test.jamon.Middle;
import test.jamon.Parent;

/**
 * Test Jamon's template inheritance
 **/

public class InheritanceTest
    extends TestBase
{
    public void testParentCall()
        throws Exception
    {
        assertTrue(Modifier.isAbstract(Parent.class.getModifiers()));
    }

    public void testChildSettingParentOptionalArg()
        throws Exception
    {
        Child child = new Child();
        child.setOpt2("o2j");
        child.render(getWriter(), 0, new Integer(1), "s", true);
        String childString = "{s 0 true o1c o5p o7c}";
        checkOutput("0 1 " + childString + " "
                    + childString + " o1c o2j o3p o4p o5p o6p");
    }

    public void testChildWithDefaults()
        throws Exception
    {
        new Child().render(getWriter(), 0, new Integer(1), "s", true);
        String childString = "{s 0 true o1c o5p o7c}";
        checkOutput("0 1 " + childString + " "
                    + childString + " o1c o2p o3p o4p o5p o6p");
    }

    public void testGrandchildWithDefaults()
        throws Exception
    {
        new Grandchild()
            .render(getWriter(), 0, new Integer(1), "s", true, Boolean.FALSE);
        String childString = "{s 0 true {s 1 false o1g o3g o5m o6p o7g o9m o10g} o1g o2m o5m o6p o7g o8m o9m}";
        checkOutput("0 1 " + childString + " " + childString + " o1g o2m o3g o4p o5m o6p");
    }

    public void testChildWithDefaultsViaParentRenderer()
        throws Exception
    {
        new Child()
            .makeParentRenderer("s", true)
            .render(getWriter(), 0, new Integer(1));
        String childString = "{s 0 true o1c o5p o7c}";
        checkOutput("0 1 " + childString + " "
                    + childString + " o1c o2p o3p o4p o5p o6p");
    }

    public void testChildSettingParentOptionalArgViaParentRenderer()
        throws Exception
    {
        new Child()
            .makeParentRenderer("s", true)
            .setOpt2("o2j")
            .render(getWriter(), 0, new Integer(1));
        String childString = "{s 0 true o1c o5p o7c}";
        checkOutput("0 1 " + childString + " "
                    + childString + " o1c o2j o3p o4p o5p o6p");
    }

    public void testMiddleParentRenderSettingParentOptionalArg()
        throws Exception
    {
        new Grandchild()
            .makeParentRenderer(Boolean.FALSE)
            .setOpt2("o2j")
            .makeParentRenderer("s", true)
            .render(getWriter(), 0, new Integer(1));
        String childString = "{s 0 true {s 1 false o1g o3g o5m o6p o7g o9m o10g} o1g o2j o5m o6p o7g o8m o9m}";
        checkOutput("0 1 " + childString + " " + childString + " o1g o2j o3g o4p o5m o6p");
    }

    public void testGrandchildWithDefaultsViaParentRenderer()
        throws Exception
    {
        new Grandchild()
            .makeParentRenderer(Boolean.FALSE)
            .makeParentRenderer("s", true)
            .render(getWriter(), 0, new Integer(1));
        String childString = "{s 0 true {s 1 false o1g o3g o5m o6p o7g o9m o10g} o1g o2m o5m o6p o7g o8m o9m}";
        checkOutput("0 1 " + childString + " " + childString + " o1g o2m o3g o4p o5m o6p");
    }

    public void testFragmentCall()
        throws Exception
    {
        new ChildFragmentCaller().render(getWriter());
        checkOutput("s0{s - 1 - g t}2");
    }

    public void testParentAnnotations()
    {
        Template templateAnnotation = Parent.class.getAnnotation(Template.class);
        NameType.checkArgs(
            templateAnnotation.requiredArguments(),
            new NameType("i", "int"), new NameType("j", "Integer"));
        NameType.checkArgSet(
            templateAnnotation.optionalArguments(),
            new NameType("opt1", "String"), new NameType("opt2", "String"),
            new NameType("opt3", "String"), new NameType("opt4", "String"),
            new NameType("opt5", "String"), new NameType("opt6", "String"));
        assertEquals(0, templateAnnotation.fragmentArguments().length);
    }

    public void testChildAnnotations()
    {
        Template templateAnnotation = Child.class.getAnnotation(Template.class);
        NameType.checkArgs(
            templateAnnotation.requiredArguments(),
            new NameType("i", "int"), new NameType("j", "Integer"),
            new NameType("a", "String"), new NameType("k", "boolean"));
        NameType.checkArgSet(
            templateAnnotation.optionalArguments(),
            new NameType("opt1", "String"), new NameType("opt2", "String"),
            new NameType("opt3", "String"), new NameType("opt4", "String"),
            new NameType("opt5", "String"), new NameType("opt6", "String"),
            new NameType("opt7", "String"));
        assertEquals(0, templateAnnotation.fragmentArguments().length);
    }

    public void testMiddleAnnotations()
    {
        Template templateAnnotation = Middle.class.getAnnotation(Template.class);
        NameType.checkArgs(
            templateAnnotation.requiredArguments(),
            new NameType("i", "int"), new NameType("j", "Integer"),
            new NameType("a", "String"), new NameType("b", "boolean"));
        NameType.checkArgSet(
            templateAnnotation.optionalArguments(),
            new NameType("opt1", "String"), new NameType("opt2", "String"),
            new NameType("opt3", "String"), new NameType("opt4", "String"),
            new NameType("opt5", "String"), new NameType("opt6", "String"),
            new NameType("opt7", "String"), new NameType("opt8", "String"),
            new NameType("opt9", "String"));
        assertEquals(0, templateAnnotation.fragmentArguments().length);
    }

    public void testGrandchildAnnotations()
    {
        Template templateAnnotation = Grandchild.class.getAnnotation(Template.class);
        NameType.checkArgs(
            templateAnnotation.requiredArguments(),
            new NameType("i", "int"), new NameType("j", "Integer"),
            new NameType("a", "String"), new NameType("b", "boolean"),
            new NameType("x", "Boolean"));
        NameType.checkArgSet(
            templateAnnotation.optionalArguments(),
            new NameType("opt1", "String"), new NameType("opt2", "String"),
            new NameType("opt3", "String"), new NameType("opt4", "String"),
            new NameType("opt5", "String"), new NameType("opt6", "String"),
            new NameType("opt7", "String"), new NameType("opt8", "String"),
            new NameType("opt9", "String"), new NameType("opt10", "String"));
        assertEquals(0, templateAnnotation.fragmentArguments().length);
    }

    public void testTemplateCaching() throws Exception
    {
        new Child1().render(getWriter());
        checkOutput("10");
        resetWriter();
        new Child2().render(getWriter());
        checkOutput("01");
    }

    public void testRecompilation()
        throws Exception
    {
        new Child(getRecompilingTemplateManager())
            .render(getWriter(), 0, new Integer(1), "s", true);
    }

    public void testHiddenParentRequiredArgs()
        throws Exception
    {
        checkCompilationFailure("/test/jamon/broken/RequiredArgSnoopingChild");
    }

    public void testHiddenParentOptionalArgs()
        throws Exception
    {
        checkCompilationFailure("/test/jamon/broken/OptionalArgSnoopingChild");
    }

    public void testHiddenParentFragmentArgs()
        throws Exception
    {
        checkCompilationFailure("/test/jamon/broken/FragmentArgSnoopingChild");
    }

    public void testLibOnlyParentFragment()
        throws Exception
    {
        getRecompilingTemplateManager().constructProxy
            ("/test/jamon/external/ChildFragment");
    }

    public void testNonParentChildCall()
        throws Exception
    {
        expectParserError("NonParentChildCall",
                          "<& *CHILD &> cannot be called from a template without an <%abstract> tag",
                          1, 1);
    }

    public void testMultipleParents()
        throws Exception
    {
        expectParserError("MultipleParents",
                          "a template cannot extend multiple templates",
                          2, 1);
    }

    private void checkCompilationFailure(String p_path)
    {
        try
        {
            getRecompilingTemplateManager().constructProxy(p_path);
            fail("recompilation of Troubled child threw no exception");
        }
        catch(TemplateCompilationException e)
        {
            // Excellent, Smithers.
        }
        catch(NoClassDefFoundError e)
        {
            // Yikes.  If we're compiling with jikes, then we might
            // not get an error.
        }
    }
}
