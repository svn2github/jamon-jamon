package org.jamon;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;

class FargInfo
{
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
