package org.modusponens.jtt;

import java.util.Iterator;
import java.util.ArrayList;

class FargInfo
{
    public FargInfo(String p_name)
    {
        m_name = p_name;
    }

    public Iterator getArgumentNames()
    {
        return new ArrayList().iterator();
    }

    public String getArgumentType(String p_name)
    {
        return null;
    }

    public String getFargInterfaceName()
    {
        return Fragment.class.getName();
    }

    public String getName()
    {
        return m_name;
    }

    private final String m_name;
}
