package org.jamon.html;

public abstract class AbstractInput
{
    protected AbstractInput(String p_name)
    {
        m_name = p_name;
    }

    public String getName()
    {
        return m_name;
    }

    private final String m_name;
}
