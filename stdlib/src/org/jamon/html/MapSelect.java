package org.jamon.html;

import java.util.Iterator;
import java.util.Map;

public class MapSelect
    extends AbstractSelect
{
    public MapSelect(String p_name, Map p_options, Object p_default)
    {
        super(p_name);
        m_options = p_options;
        m_default = p_default;
    }

    public MapSelect(String p_name, Map p_options)
    {
        this(p_name, p_options, null);
    }

    public Iterator getValues()
    {
        return m_options.keySet().iterator();
    }

    public Object getRenderable(Object p_value)
    {
        return m_options.get(p_value);
    }

    public boolean isSelected(Object p_value)
    {
        return m_default != null && m_default.equals(p_value);
    }

    private final Map m_options;
    private final Object m_default;
}
