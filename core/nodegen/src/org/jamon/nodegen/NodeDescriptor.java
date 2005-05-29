package org.jamon.nodegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class NodeDescriptor
{
    public NodeDescriptor(String p_line, Map<String, NodeDescriptor> p_nodes)
    {
        StringTokenizer tokenizer = new StringTokenizer(p_line, " ");
        m_name = tokenizer.nextToken();
        int index;
        if ((index = m_name.indexOf(':')) >= 0)
        {
            m_parent = m_name.substring(index + 1);
            m_parentMembers = p_nodes.get(m_parent).getAllMembers();
            m_name = m_name.substring(0, index);
        }
        else
        {
            m_parent = "AbstractNode";
            m_parentMembers = new ArrayList<NodeMember>();
        }

        m_members = new ArrayList<NodeMember>();
        while (tokenizer.hasMoreTokens())
        {
            m_members.add(new NodeMember(tokenizer.nextToken()));
        }
    }

    public List<NodeMember> getAllMembers()
    {
        ArrayList<NodeMember> allMembers = 
            new ArrayList<NodeMember>(m_parentMembers);
        allMembers.addAll(m_members);
        return allMembers;
    }
    
    public List<NodeMember> getMembers()
    {
        return m_members;
    }

    public String getName()
    {
        return m_name;
    }

    public List<NodeMember> getParentMembers()
    {
        return m_parentMembers;
    }

    public String getParent()
    {
        return m_parent;
    }
    
    private List<NodeMember> m_parentMembers;
    private String m_name;
    private List<NodeMember> m_members;
    private String m_parent;
    
}
