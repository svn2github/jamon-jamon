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
import java.util.Iterator;
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

    public TemplateDescription(TemplateUnit p_templateUnit)
        throws JamonException
    {
        m_requiredArgs = new LinkedList();
        addAll(m_requiredArgs, p_templateUnit.getSignatureRequiredArgs() );
        m_optionalArgs = new HashSet();
        addAll(m_optionalArgs, p_templateUnit.getSignatureOptionalArgs() );
        m_signature = p_templateUnit.getSignature();
        m_fragmentInterfaces = p_templateUnit.getFragmentArgsList();
    }

    public TemplateDescription(Class p_intf)
        throws JamonException
    {
        try
        {
            String[] requiredArgNames =
                (String[]) p_intf.getField("REQUIRED_ARG_NAMES").get(null);
            String[] requiredArgTypes =
                (String[]) p_intf.getField("REQUIRED_ARG_TYPES").get(null);
            m_requiredArgs = new LinkedList();
            for(int i = 0; i < requiredArgNames.length; i++)
            {
                m_requiredArgs.add
                    (new RequiredArgument(requiredArgNames[i],
                                          requiredArgTypes[i]));
            }

            String[] optionalArgNames =
                (String[]) p_intf.getField("OPTIONAL_ARG_NAMES").get(null);
            String[] optionalArgTypes =
                (String[]) p_intf.getField("OPTIONAL_ARG_TYPES").get(null);
            m_optionalArgs = new HashSet();
            for(int i = 0; i < optionalArgNames.length; i++)
            {
                m_optionalArgs.add
                    (new OptionalArgument(optionalArgNames[i],
                                          optionalArgTypes[i],
                                          null));
            }

            m_signature = (String) p_intf.getField("SIGNATURE").get(null);

            m_fragmentInterfaces = new LinkedList();
            String[] fragmentArgNames =
                (String []) p_intf.getField("FRAGMENT_ARG_NAMES").get(null);
            for (int i = 0; i < fragmentArgNames.length; i++)
            {
                FragmentUnit fragmentUnit =
                    new FragmentUnit(fragmentArgNames[i], null);
                String[] argNames = (String[])
                    p_intf
                    .getField("FARGINFO_" + fragmentArgNames[i] + "_ARG_NAMES")
                    .get(null);
                String[] argTypes = (String[])
                    p_intf
                    .getField("FARGINFO_" + fragmentArgNames[i] + "_ARG_TYPES")
                    .get(null);
                for(int j = 0; j < argNames.length; j++)
                {
                    fragmentUnit.addRequiredArg
                        (new RequiredArgument(argNames[j], argTypes[j]));
                }
                m_fragmentInterfaces.add(new FragmentArgument(fragmentUnit));
            }

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

    private static void addAll(Collection p_collection, Iterator p_iter)
    {
        while (p_iter.hasNext())
        {
            p_collection.add(p_iter.next());
        }
    }
}
