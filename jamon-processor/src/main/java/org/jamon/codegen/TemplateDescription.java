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

package org.jamon.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;

import org.jamon.util.AnnotationReflector;
import org.jamon.annotations.Argument;
import org.jamon.annotations.Fragment;
import org.jamon.annotations.Method;
import org.jamon.annotations.Template;

public class TemplateDescription
{
    private final List<RequiredArgument> m_requiredArgs;
    private final Set<OptionalArgument> m_optionalArgs;
    private final String m_signature;
    private final List<FragmentArgument> m_fragmentInterfaces;
    private final Map<String, MethodUnit> m_methodUnits;
    private final int m_inheritanceDepth;
    private final Collection<String> m_abstractMethodNames;
    private final int m_genericParamsCount;
    private final String m_jamonContextType;
    private final boolean m_replaceable;

    public final static TemplateDescription EMPTY = new TemplateDescription();

    private TemplateDescription()
    {
        m_requiredArgs = Collections.emptyList();
        m_optionalArgs = Collections.emptySet();
        m_signature = null;
        m_fragmentInterfaces = Collections.emptyList();
        m_methodUnits = Collections.emptyMap();
        m_inheritanceDepth = -1;
        m_abstractMethodNames = Collections.emptyList();
        m_genericParamsCount = 0;
        m_jamonContextType = null;
        m_replaceable = false;
    }

    public TemplateDescription(TemplateUnit p_templateUnit)
    {
        m_requiredArgs = new LinkedList<RequiredArgument>();
        m_requiredArgs.addAll(p_templateUnit.getSignatureRequiredArgs());
        m_optionalArgs = new HashSet<OptionalArgument>();
        m_optionalArgs.addAll(p_templateUnit.getSignatureOptionalArgs());
        for (OptionalArgument arg : m_optionalArgs)
        {
            arg.setDefault(null);
        }
        m_signature = p_templateUnit.getSignature();
        m_fragmentInterfaces = p_templateUnit.getFragmentArgs();
        m_methodUnits = new HashMap<String, MethodUnit>();
        for (MethodUnit methodUnit: p_templateUnit.getSignatureMethodUnits())
        {
            m_methodUnits.put(methodUnit.getName(), methodUnit);
        }
        m_inheritanceDepth = p_templateUnit.getInheritanceDepth();
        m_abstractMethodNames = p_templateUnit.getAbstractMethodNames();
        m_genericParamsCount = p_templateUnit.getGenericParams().getCount();
        m_jamonContextType = p_templateUnit.getJamonContextType();
        m_replaceable = p_templateUnit.isReplaceable();
    }

    public TemplateDescription(Class<?> p_proxy) throws NoSuchFieldException
    {
        Template templateAnnotation =
            new AnnotationReflector(p_proxy).getAnnotation(Template.class);
        if (templateAnnotation == null)
        {
            //FIXME - throw something more sensical here
            throw new NoSuchFieldException(
                "class " + p_proxy.getName() + " lacks a template annotation");
        }

        m_requiredArgs = getRequiredArgs(templateAnnotation.requiredArguments());
        m_optionalArgs = getOptionalArgs(templateAnnotation.optionalArguments());

        m_fragmentInterfaces = getFragmentArguments(
            templateAnnotation.fragmentArguments(),
            new TemplateUnit(PathUtils.getPathForProxyClass(p_proxy), null));
        m_methodUnits = new HashMap<String, MethodUnit>();
        for (Method methodAnnotation: templateAnnotation.methods())
        {
            DeclaredMethodUnit method =
                new DeclaredMethodUnit(methodAnnotation.name(), null, null, null);
            for (Argument argument: methodAnnotation.requiredArguments())
            {
                method.addRequiredArg(makeRequiredArg(argument));
            }
            for (Argument argument: methodAnnotation.optionalArguments())
            {
                method.addOptionalArg(makeOptionalArg(argument));
            }
            for (Fragment fragment: methodAnnotation.fragmentArguments())
            {
                method.addFragmentArg(makeFragmentArg(method, fragment));
            }
            m_methodUnits.put(method.getName(), method);
        }
        m_abstractMethodNames = Arrays.asList(templateAnnotation.abstractMethodNames());
        m_signature = templateAnnotation.signature();
        m_inheritanceDepth = templateAnnotation.inheritanceDepth();
        m_genericParamsCount = templateAnnotation.genericsCount();
        m_jamonContextType = nullToEmptyString(templateAnnotation.jamonContextType());
        m_replaceable = templateAnnotation.replaceable();
    }

    private String nullToEmptyString(String jamonContextType)
    {
        return jamonContextType.equals("") ? null : jamonContextType;
    }

    private static List<RequiredArgument> getRequiredArgs(Argument[] p_arguments)
    {
        List<RequiredArgument> args = new ArrayList<RequiredArgument>(p_arguments.length);
        for (Argument argument: p_arguments) {
            args.add(makeRequiredArg(argument));
        }
        return args;
    }

    private static Set<OptionalArgument> getOptionalArgs(Argument[] p_arguments)
    {
        Set<OptionalArgument> args = new HashSet<OptionalArgument>();
        for (Argument argument: p_arguments) {
            args.add(makeOptionalArg(argument));
        }
        return args;
    }

    private static RequiredArgument makeRequiredArg(Argument argument)
    {
        return new RequiredArgument(argument.name(), argument.type(), null);
    }

    private static OptionalArgument makeOptionalArg(Argument argument)
    {
        return new OptionalArgument(argument.name(), argument.type(), null);
    }

    private static List<FragmentArgument> getFragmentArguments(
        Fragment[] p_fragments, Unit p_parentUnit)
    {
        List<FragmentArgument> fragmentArguments = new ArrayList<FragmentArgument>(p_fragments.length);
        for (Fragment fragment: p_fragments)
        {
            fragmentArguments.add(makeFragmentArg(p_parentUnit, fragment));
        }
        return fragmentArguments;
    }

    private static FragmentArgument makeFragmentArg(Unit p_parentUnit, Fragment p_fragment)
    {
        FragmentUnit fragmentUnit =
            new FragmentUnit(p_fragment.name(), p_parentUnit, new GenericParams(), null, null);
        for (Argument argument: p_fragment.requiredArguments()) {
            fragmentUnit.addRequiredArg(makeRequiredArg(argument));
        }
        FragmentArgument fragmentArgument = new FragmentArgument(fragmentUnit, null);
        return fragmentArgument;
    }

    public List<RequiredArgument> getRequiredArgs()
    {
        return m_requiredArgs;
    }

    public Set<OptionalArgument> getOptionalArgs()
    {
        return m_optionalArgs;
    }

    /**
     * Get the signature hash for the described template. The signature is a
     * hash which will change in the event that the template's API has changed.
     * @return the signature hash for this template
     */
    public String getSignature()
    {
        return m_signature;
    }

    public List<FragmentArgument> getFragmentInterfaces()
    {
        return m_fragmentInterfaces;
    }

    public Map<String, MethodUnit> getMethodUnits()
    {
        return m_methodUnits;
    }

    public Collection<String> getAbstractMethodNames()
    {
        return m_abstractMethodNames;
    }

    public int getInheritanceDepth()
    {
        return m_inheritanceDepth;
    }

    public int getGenericParamsCount()
    {
        return m_genericParamsCount;
    }

    public String getJamonContextType()
    {
        return m_jamonContextType;
    }

    public boolean isReplaceable()
    {
        return m_replaceable;
    }
}

