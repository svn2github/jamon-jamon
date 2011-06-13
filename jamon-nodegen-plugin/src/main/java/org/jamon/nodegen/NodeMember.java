package org.jamon.nodegen;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


class NodeMember
{
    NodeMember(String p_descriptor)
    {
        int index = p_descriptor.indexOf(':');
        m_type = p_descriptor.substring(0, index);
        if (p_descriptor.endsWith("*"))
        {
            m_name =
                p_descriptor.substring(
                    index + 1,
                    p_descriptor.length() - 1);
            m_isList = true;
        }
        else
        {
            m_name = p_descriptor.substring(index + 1);
            m_isList = false;
        }
    }

    public String instanceName()
    {
        return "m_" + m_name + (m_isList ? "s" : "");
    }

    public boolean isPrimative()
    {
        return "char".equals(m_type);
    }

    public String getGetter()
    {
        return m_isList
            ? "get" + getCapitalizedName() + "s()"
            : "get" + getCapitalizedName() + "()";
    }

    public String getCapitalizedName()
    {
        return m_name.substring(0, 1).toUpperCase(Locale.US) + m_name.substring(1);
    }

    public String hashCodeExpr()
    {
        if ("char".equals(m_type))
        {
            return instanceName();
        }
        else
        {
            return instanceName() + ".hashCode()";
        }
    }

    public boolean isList()
    {
        return m_isList;
    }

    public boolean isNode()
    {
        return !isPrimative() && !NON_NODE_TYPES.contains(m_type);
    }

    public String getName()
    {
        return m_name;
    }

    public String getType()
    {
        return m_type;
    }

    private final String m_type;
    private final String m_name;
    private final boolean m_isList;

    private final static Set<String> NON_NODE_TYPES = Collections.unmodifiableSet(
      new HashSet<String>(Arrays.asList("String", "org.jamon.codegen.AnnotationType")));
}
