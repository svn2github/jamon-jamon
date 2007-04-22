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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jamon.ParserErrorsImpl;
import org.jamon.node.ArgValueNode;
import org.jamon.node.ParentArgNode;
import org.jamon.node.ParentArgWithDefaultNode;

public class InheritedArgs
{
    public InheritedArgs(String p_unitName,
                         String p_parentName,
                         Collection<RequiredArgument> p_requiredArgs,
                         Collection<OptionalArgument> p_optionalArgs,
                         Collection<FragmentArgument> p_fragmentArgs,
                         ParserErrorsImpl p_errors)
    {
        m_parentName = p_parentName;
        m_errors = p_errors;
        m_requiredArgs = p_requiredArgs;
        m_optionalArgs = p_optionalArgs;
        m_fragmentArgs = p_fragmentArgs;
    }

    private final String m_parentName;
    private final ParserErrorsImpl m_errors;
    private final Set<AbstractArgument> m_visibleArgs =
        new HashSet<AbstractArgument>();
    private final Collection<RequiredArgument> m_requiredArgs;
    private final Collection<OptionalArgument> m_optionalArgs;
    private final Collection<FragmentArgument> m_fragmentArgs;
    private final Map<OptionalArgument, String> m_defaultOverrides =
        new HashMap<OptionalArgument, String>();

    public Collection<AbstractArgument> getVisibleArgs()
    {
        return m_visibleArgs;
    }

    public boolean isArgVisible(AbstractArgument p_arg)
    {
        return m_visibleArgs.contains(p_arg);
    }

    public void addParentArg(ParentArgNode p_node)
    {
        String name = p_node.getName().getName();
        ArgValueNode value = (p_node instanceof ParentArgWithDefaultNode)
            ? ((ParentArgWithDefaultNode) p_node).getValue()
            : null;
        for (RequiredArgument arg : m_requiredArgs)
        {
            if(arg.getName().equals(name))
            {
                if (value == null)
                {
                    m_visibleArgs.add(arg);
                }
                else
                {
                    m_errors.addError(
                        name + " is an inherited required argument, and may not be given a default value",
                        value.getLocation());
                }
                return;
            }
        }
        for (OptionalArgument arg : m_optionalArgs)
        {
            if(arg.getName().equals(name))
            {
                if (value != null)
                {
                    m_defaultOverrides.put(arg, value.getValue().trim());
                }
                m_visibleArgs.add(arg);
                return;
            }
        }
        for (FragmentArgument arg : m_fragmentArgs)
        {
            if(arg.getName().equals(name))
            {
                if (value == null)
                {
                    m_visibleArgs.add(arg);
                }
                else
                {
                    m_errors.addError(
                        name + " is an inherited fragment argument, and may not be given a default value",
                        value.getLocation());
                }
                return;
            }
        }
        m_errors.addError(
            m_parentName + " does not have an arg named " + name,
             p_node.getName().getLocation());
    }

    public String getDefaultValue(OptionalArgument p_arg)
    {
        return m_defaultOverrides.get(p_arg);
    }

    public Collection<OptionalArgument> getOptionalArgsWithNewDefaultValues()
    {
        return m_defaultOverrides.keySet();
    }
}
