package org.modusponens.jtt;

public class Encoding
{
    private Encoding(String p_name)
    {
        m_name = p_name;
    }
    public String toString()
    {
        return m_name;
    }
    public boolean equals(Object p_obj)
    {
        return p_obj == this;
    }

    private final String m_name;

    final static Encoding DEFAULT = new Encoding("");
    final static Encoding NONE = new Encoding("Un");
    final static Encoding HTML = new Encoding("Html");
    final static Encoding XML = new Encoding("Xml");
    final static Encoding URL = new Encoding("Url");
}
