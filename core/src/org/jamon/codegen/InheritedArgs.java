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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class InheritedArgs
{
    public InheritedArgs(String p_unitName,
                         String p_parentName,
                         Collection p_requiredArgs,
                         Collection p_optionalArgs,
                         Collection p_fragmentArgs)
    {
        m_name = p_unitName;
        m_parentName = p_parentName;
        m_requiredArgs = p_requiredArgs;
        m_optionalArgs = p_optionalArgs;
        m_fragmentArgs = p_fragmentArgs;
    }

    private final String m_name;
    private final String m_parentName;
    private final Set m_visibleArgs = new HashSet();
    private final Collection m_requiredArgs;
    private final Collection m_optionalArgs;
    private final Collection m_fragmentArgs;

    public Iterator getVisibleArgs()
    {
        return m_visibleArgs.iterator();
    }

    public boolean isArgVisible(AbstractArgument p_arg)
    {
        return m_visibleArgs.contains(p_arg);
    }

    public void addParentArg(String p_name, String p_default)
    {
        for (Iterator i = m_requiredArgs.iterator(); i.hasNext(); )
        {
            RequiredArgument arg = (RequiredArgument) i.next();
            if(arg.getName().equals(p_name))
            {
                if(p_default != null)
                {
                    //FIXME - unit test this.
                    throw new TunnelingException
                        (m_name
                         + " gives a default value to inherited required argument "
                         + p_name);
                }
                m_visibleArgs.add(arg);
                return;
            }
        }

        for (Iterator i = m_optionalArgs.iterator(); i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            if(arg.getName().equals(p_name))
            {
                arg.setDefault(p_default);
                m_visibleArgs.add(arg);
                return;
            }
        }

        for (Iterator i = m_fragmentArgs.iterator(); i.hasNext(); )
        {
            FragmentArgument arg = (FragmentArgument) i.next();
            if (arg.getName().equals(p_name))
            {
                if(p_default != null)
                {
                    //FIXME - unit test this.
                    throw new TunnelingException
                        (m_name
                         + " gives a default value to inherited fragment argument "
                         + p_name);
                }
                m_visibleArgs.add(arg);
                return;
            }
        }

        throw new TunnelingException
            (m_name + " mistakenly thinks that " + m_parentName
             + " has an arg named "  + p_name);
    }
}
