package org.modusponens.jtt;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;

class FargInfo
{
    public FargInfo(String p_name, String p_intfName, Map p_args)
    {
        m_name = p_name;
        m_intfName = p_intfName;
        m_args = p_args;
    }

    public Iterator getArgumentNames()
    {
        return m_args.keySet().iterator();
    }

    public String getArgumentType(String p_name)
    {
        return (String) m_args.get(p_name);
    }

    public String getFargInterfaceName()
    {
        return m_intfName;
    }

    public String getName()
    {
        return m_name;
    }

    private final String m_name;
    private final String m_intfName;
    private final Map m_args;
}
