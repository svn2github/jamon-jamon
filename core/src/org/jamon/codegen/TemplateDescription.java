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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;

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
    }

    public TemplateDescription(TemplateUnit p_templateUnit)
    {
        m_requiredArgs = new LinkedList<RequiredArgument>();
        addAll(m_requiredArgs, p_templateUnit.getSignatureRequiredArgs() );
        m_optionalArgs = new HashSet<OptionalArgument>();
        addAll(m_optionalArgs, p_templateUnit.getSignatureOptionalArgs() );
        for (OptionalArgument arg : m_optionalArgs)
        {
            arg.setDefault(null);
        }
        m_signature = p_templateUnit.getSignature();
        m_fragmentInterfaces = p_templateUnit.getFragmentArgsList();
        m_methodUnits = new HashMap<String, MethodUnit>();
        for (Iterator<MethodUnit> i = p_templateUnit.getSignatureMethodUnits();
             i.hasNext(); )
        {
            MethodUnit methodUnit = i.next();
            m_methodUnits.put(methodUnit.getName(), methodUnit);
        }
        m_inheritanceDepth = p_templateUnit.getInheritanceDepth();
        m_abstractMethodNames = p_templateUnit.getAbstractMethodNames();
        m_genericParamsCount = p_templateUnit.getGenericParams().getCount();
    }

    public TemplateDescription(Class p_intf)
        throws NoSuchFieldException, IllegalAccessException
    {
        m_requiredArgs = getRequiredArgs(p_intf, "");
        m_optionalArgs = getOptionalArgs(p_intf, "");
        m_fragmentInterfaces =
            getFragmentArgs(p_intf, "", new TemplateUnit(null, null));
        m_methodUnits = new HashMap<String, MethodUnit>();
        String[] methodNames = getStringArray(p_intf, "METHOD_NAMES");
        for (int i = 0; i < methodNames.length; i++)
        {
            DeclaredMethodUnit method =
                new DeclaredMethodUnit(methodNames[i], null, null);
            String prefix = "METHOD_" + methodNames[i] + "_";
            for (Iterator<RequiredArgument> j =
                     getRequiredArgs(p_intf, prefix).iterator();
                 j.hasNext(); )
            {
                method.addRequiredArg(j.next());
            }
            for (Iterator<OptionalArgument> j =
                    getOptionalArgs(p_intf, prefix).iterator();
                 j.hasNext(); )
            {
                method.addOptionalArg(j.next());
            }
            for (Iterator<FragmentArgument> j =
                    getFragmentArgs(p_intf, prefix, method).iterator();
                 j.hasNext(); )
            {
                method.addFragmentArg(j.next());
            }
            m_methodUnits.put(method.getName(), method);
        }
        m_abstractMethodNames =
            Arrays.asList(getStringArray(p_intf, "ABSTRACT_METHOD_NAMES"));
        m_signature = (String) p_intf.getField("SIGNATURE").get(null);
        m_inheritanceDepth =
            ((Integer) p_intf.getField("INHERITANCE_DEPTH").get(null));
        m_genericParamsCount =
            ((Integer) p_intf.getField("GENERICS_COUNT").get(null));
    }

    private static List<RequiredArgument> getRequiredArgs(
        Class p_class, String p_prefix)
        throws NoSuchFieldException, IllegalAccessException
    {
        List<RequiredArgument> args = new LinkedList<RequiredArgument>();
        String[] requiredArgNames =
            getStringArray(p_class, p_prefix + "REQUIRED_ARG_NAMES");
        String[] requiredArgTypes =
            getStringArray(p_class, p_prefix + "REQUIRED_ARG_TYPES");
        for(int i = 0; i < requiredArgNames.length; i++)
        {
            args.add(new RequiredArgument(
                requiredArgNames[i], requiredArgTypes[i], null));
        }
        return args;
    }

    private static Set<OptionalArgument> getOptionalArgs(
        Class p_class, String p_prefix)
        throws NoSuchFieldException, IllegalAccessException
    {
        Set<OptionalArgument> args = new HashSet<OptionalArgument>();
        String[] optionalArgNames =
            getStringArray(p_class, p_prefix + "OPTIONAL_ARG_NAMES");
        String[] optionalArgTypes =
            getStringArray(p_class, p_prefix + "OPTIONAL_ARG_TYPES");
        for(int i = 0; i < optionalArgNames.length; i++)
        {
            args.add(new OptionalArgument(optionalArgNames[i],
                                          optionalArgTypes[i],
                                          null));
        }
        return args;
    }

    private static List<FragmentArgument> getFragmentArgs(
        Class p_class, String p_prefix, Unit p_parentUnit)
        throws NoSuchFieldException, IllegalAccessException
    {
        List<FragmentArgument> fragmentArgs = new LinkedList<FragmentArgument>();
        String[] fragmentArgNames =
            getStringArray(p_class, p_prefix + "FRAGMENT_ARG_NAMES");
        for (int i = 0; i < fragmentArgNames.length; i++)
        {
            FragmentUnit frag =new FragmentUnit(
                fragmentArgNames[i], p_parentUnit, new GenericParams(), null);
            String[] fragmentArgArgNames = getStringArray
                (p_class,
                 p_prefix + "FRAGMENT_ARG_"
                 + fragmentArgNames[i] + "_ARG_NAMES");
            for(int j = 0; j < fragmentArgArgNames.length; j++)
            {
                frag.addRequiredArg
                    (new RequiredArgument(fragmentArgArgNames[j], null, null));
            }
            fragmentArgs.add(new FragmentArgument(frag, null));
        }
        return fragmentArgs;
    }

    private static String[] getStringArray(Class p_class, String p_fieldName)
        throws NoSuchFieldException, IllegalAccessException
    {
        return (String[]) p_class.getField(p_fieldName).get(null);
    }

    public List<RequiredArgument> getRequiredArgs()
    {
        return m_requiredArgs;
    }

    public Set<OptionalArgument> getOptionalArgs()
    {
        return m_optionalArgs;
    }

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

    private static<T> void addAll(Collection<T> p_collection, Iterator<T> p_iter)
    {
        while (p_iter.hasNext())
        {
            p_collection.add(p_iter.next());
        }
    }
}
