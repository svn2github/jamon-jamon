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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class FargInfo
{
    public FargInfo(String p_name, UnitInfo p_unitInfo)
    {
        m_name = p_name;
        m_args = new LinkedList();
        for (Iterator i = p_unitInfo.getRequiredArgs(); i.hasNext(); /* */)
        {
            m_args.add((Argument) i.next());
        }
    }

    public FargInfo(String p_name, Iterator p_argNames, Map p_args)
    {
        m_name = p_name;
        m_args = new LinkedList();
        while (p_argNames.hasNext())
        {
            String name = (String) p_argNames.next();
            m_args.add(new Argument(name, (String) p_args.get(name)));
        }
    }

    public Iterator getArgs()
    {
        return m_args.iterator();
    }

    public void printArgsDecl(IndentingWriter p_writer)
    {
        for(Iterator i = getArgs(); i.hasNext(); /* */)
        {
            Argument arg = (Argument) i.next();
            p_writer.print("final " + arg.getType() + " " + arg.getName());
            if (i.hasNext())
            {
                p_writer.print(", ");
            }
        }
    }

    public void printArgs(IndentingWriter p_writer)
    {
        for(Iterator i = getArgs(); i.hasNext(); /* */)
        {
            p_writer.print(((Argument) i.next()).getName());
            if (i.hasNext())
            {
                p_writer.print(", ");
            }
        }
    }

    public String getFargInterfaceName()
    {
        return "Fragment_" + m_name;
    }

    public String getName()
    {
        return m_name;
    }

    private final String m_name;
    private final List m_args;
}
