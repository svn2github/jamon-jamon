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
 * Contributor(s):
 */

package org.jamon.codegen;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class FargInfo
{
    public FargInfo(String p_name, UnitInfo p_unitInfo)
    {
        m_name = p_name;
        m_args = new HashMap();
        m_argNames = new ArrayList();
        for(Iterator i = p_unitInfo.getRequiredArgs(); i.hasNext(); /**/)
        {
            Argument arg = (Argument) i.next();
            m_argNames.add(arg.getName());
            m_args.put(arg.getName(), arg.getType());
        }
    }

    public FargInfo(String p_name, Iterator p_argNames, Map p_args)
    {
        m_name = p_name;
        m_args = p_args;
        m_argNames = new ArrayList();
        while (p_argNames.hasNext())
        {
            m_argNames.add(p_argNames.next());
        }
    }

    public Iterator getArgumentNames()
    {
        return m_argNames.iterator();
    }

    public String getArgumentType(String p_name)
    {
        return (String) m_args.get(p_name);
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
    private final Map m_args;
    private final ArrayList m_argNames;
}
