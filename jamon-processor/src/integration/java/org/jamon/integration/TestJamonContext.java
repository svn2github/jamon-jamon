package org.jamon.integration;

public class TestJamonContext
{
    public TestJamonContext(int p_data)
    {
        m_data = p_data;
    }

    public int getData()
    {
        return m_data;
    }

    private final int m_data;
}
