package org.jamon.html;

public class Input
    extends AbstractInput
{
    public Input(String p_name)
    {
        this(p_name,null);
    }

    public Input(String p_name, Object p_value)
    {
        super(p_name);
        m_value = p_value == null ? null : p_value.toString();
    }

    public String getValue()
    {
        return m_value;
    }

    private final String m_value;
}
