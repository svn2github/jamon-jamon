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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import org.jamon.node.ADefault;

public class UnitInfo
{
    public UnitInfo(String p_name)
    {
        m_name = p_name;
    }
    private final String m_name;

    public String getName()
    {
        return m_name;
    }

    public void addFarg(String p_name, String p_type)
    {
        addArg(p_name, p_type, null);
        m_fargs.add(p_name);
    }

    public void addArg(String p_name, String p_type, ADefault p_default)
    {
        if (p_default == null)
        {
            m_requiredArgs.add(p_name);
            m_argTypes.put(p_name, p_type);
        }
        else
        {
            m_optionalArgs.add(p_name);
            m_argTypes.put(p_name,p_type);
            m_default.put(p_name,
                          p_default.getArgexpr().toString().trim());
        }
    }

    public String getArgType(String p_argName)
    {
        return (String) m_argTypes.get(p_argName);
    }

    public String getDefault(String p_argName)
    {
        return (String) m_default.get(p_argName);
    }
    public Iterator getRequiredArgNames()
    {
        return m_requiredArgs.iterator();
    }
    public Iterator getOptionalArgNames()
    {
        return m_optionalArgs.iterator();
    }
    public Iterator getFargNames()
    {
        return m_fargs.iterator();
    }
    public Map getArgumentMap()
    {
        return m_argTypes;
    }

    public void printRequiredArgsDecl(IndentingWriter p_writer)
    {
        for (Iterator i = getRequiredArgNames(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            p_writer.print("final " + getArgType(name) + " " + name);
            if (i.hasNext())
            {
                p_writer.print(", ");
            }
        }
    }

    public void printRequiredArgs(IndentingWriter p_writer)
    {
        for (Iterator i = getRequiredArgNames(); i.hasNext(); /* */)
        {
            p_writer.print((String) i.next());
            if (i.hasNext())
            {
                p_writer.print(", ");
            }
        }
    }

    private final Map m_default = new HashMap();
    private final Map m_argTypes = new HashMap();
    private final List m_requiredArgs = new LinkedList();
    private final List m_optionalArgs = new LinkedList();
    private final List m_fargs = new LinkedList();
}
