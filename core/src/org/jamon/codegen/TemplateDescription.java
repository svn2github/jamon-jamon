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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;

import org.jamon.JamonException;

public class TemplateDescription
{
    private final List m_requiredArgs;
    private final Set m_optionalArgs;
    private final String m_signature;
    private final List m_fragmentInterfaces;
    private final Map m_methodUnits;

    public final static TemplateDescription EMPTY = new TemplateDescription();

    private TemplateDescription()
    {
        m_requiredArgs = Collections.EMPTY_LIST;
        m_optionalArgs = Collections.EMPTY_SET;
        m_signature = null;
        m_fragmentInterfaces = Collections.EMPTY_LIST;
        m_methodUnits = Collections.EMPTY_MAP;
    }

    public TemplateDescription(TemplateUnit p_templateUnit)
        throws JamonException
    {
        m_requiredArgs = new LinkedList();
        addAll(m_requiredArgs, p_templateUnit.getSignatureRequiredArgs() );
        m_optionalArgs = new HashSet();
        addAll(m_optionalArgs, p_templateUnit.getSignatureOptionalArgs() );
        for (Iterator i = m_optionalArgs.iterator(); i.hasNext(); )
        {
            ((OptionalArgument) i.next()).setDefault(null);
        }
        m_signature = p_templateUnit.getSignature();
        m_fragmentInterfaces = p_templateUnit.getFragmentArgsList();
        m_methodUnits = new HashMap();
        for (Iterator i = p_templateUnit.getSignatureMethodUnits();
             i.hasNext(); )
        {
            MethodUnit methodUnit = (MethodUnit) i.next();
            m_methodUnits.put(methodUnit.getName(), methodUnit);
        }
    }

    public TemplateDescription(Class p_intf)
        throws JamonException
    {
        try
        {
            m_requiredArgs = getRequiredArgs(p_intf, "");
            m_optionalArgs = getOptionalArgs(p_intf, "");
            m_fragmentInterfaces = getFragmentArgs(p_intf, "");
            m_methodUnits = new HashMap();
            String[] methodNames = getStringArray(p_intf, "METHOD_NAMES");
            for (int i = 0; i < methodNames.length; i++)
            {
                MethodUnit method =
                    new DeclaredMethodUnit(methodNames[i], null);
                String prefix = "METHOD_" + methodNames[i] + "_";
                for (Iterator j = getRequiredArgs(p_intf, prefix).iterator();
                     j.hasNext(); )
                {
                    method.addRequiredArg((RequiredArgument) j.next());
                }
                for (Iterator j = getOptionalArgs(p_intf, prefix).iterator();
                     j.hasNext(); )
                {
                    method.addOptionalArg((OptionalArgument) j.next());
                }
                for (Iterator j = getFragmentArgs(p_intf, prefix).iterator();
                     j.hasNext(); )
                {
                    method.addFragmentArg((FragmentArgument) j.next());
                }
                m_methodUnits.put(method.getName(), method);
            }
            m_signature = (String) p_intf.getField("SIGNATURE").get(null);
        }
        catch (NoSuchFieldException e)
        {
            throw new JamonException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new JamonException(e);
        }
    }

    private static List getRequiredArgs(Class p_class, String p_prefix)
        throws NoSuchFieldException, IllegalAccessException
    {
        List args = new LinkedList();
        String[] requiredArgNames =
            getStringArray(p_class, p_prefix + "REQUIRED_ARG_NAMES");
        String[] requiredArgTypes =
            getStringArray(p_class, p_prefix + "REQUIRED_ARG_TYPES");
        for(int i = 0; i < requiredArgNames.length; i++)
        {
            args.add(new RequiredArgument(requiredArgNames[i],
                                            requiredArgTypes[i]));
        }
        return args;
    }

    private static Set getOptionalArgs(Class p_class, String p_prefix)
        throws NoSuchFieldException, IllegalAccessException
    {
        Set args = new HashSet();
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

    private static List getFragmentArgs(Class p_class, String p_prefix)
        throws NoSuchFieldException, IllegalAccessException
    {
        List fragmentArgs = new LinkedList();
        String[] fragmentArgNames =
            getStringArray(p_class, p_prefix + "FRAGMENT_ARG_NAMES");
        for (int i = 0; i < fragmentArgNames.length; i++)
        {
            FragmentUnit frag =
                new FragmentUnit(fragmentArgNames[i], null);
                String[] fragmentArgArgNames = getStringArray
                    (p_class,
                     p_prefix + "FRAGMENT_ARG_"
                     + fragmentArgNames[i] + "_ARG_NAMES");
                for(int j = 0; j < fragmentArgArgNames.length; j++)
                {
                    frag.addRequiredArg
                        (new RequiredArgument(fragmentArgArgNames[j], null));
                }
                fragmentArgs.add(new FragmentArgument(frag));
        }
        return fragmentArgs;
    }

    private static String[] getStringArray(Class p_class, String p_fieldName)
        throws NoSuchFieldException, IllegalAccessException
    {
        return (String[]) p_class.getField(p_fieldName).get(null);
    }

    public List getRequiredArgs()
    {
        return m_requiredArgs;
    }

    public Set getOptionalArgs()
    {
        return m_optionalArgs;
    }

    public String getSignature()
    {
        return m_signature;
    }

    public List getFragmentInterfaces()
    {
        return m_fragmentInterfaces;
    }

    public Map getMethodUnits()
    {
        return m_methodUnits;
    }

    private static void addAll(Collection p_collection, Iterator p_iter)
    {
        while (p_iter.hasNext())
        {
            p_collection.add(p_iter.next());
        }
    }
}
