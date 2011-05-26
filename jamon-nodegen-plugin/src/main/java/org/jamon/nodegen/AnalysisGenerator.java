package org.jamon.nodegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AnalysisGenerator
{
    private final String m_packageName;
    private final File m_targetDir;
    private final Iterable<NodeDescriptor> m_nodes;

    public AnalysisGenerator(String p_packageName, File p_targetDir, Iterable<NodeDescriptor> p_nodes)
    {
        m_packageName = p_packageName;
        m_targetDir = p_targetDir;
        m_nodes = p_nodes;
    }

    public void generateAnalysisInterface() throws IOException
    {
        final PrintWriter writer = new PrintWriter(new FileWriter(new File(m_targetDir, "Analysis.java")));
        writer.println("package " + m_packageName + ";");
        writer.println("public interface Analysis");
        writer.println("{");
        for (NodeDescriptor node : m_nodes)
        {
            String name = node.getName();
            writer.println("    void case" + name + "(" + name + " p_node);");
        }
        writer.println("}");
        writer.close();
    }

    public void generateAnalysisAdapterClass() throws IOException
    {
        final PrintWriter writer = new PrintWriter(new FileWriter(new File(m_targetDir, "AnalysisAdapter.java")));
        writer.println("package " + m_packageName + ";");
        writer.println("public class AnalysisAdapter implements Analysis");
        writer.println("{");
        for (NodeDescriptor node : m_nodes)
        {
            String name = node.getName();
            writer.println(
                "  public void case" + name + "(" + name + " p_node) {}");
        }
        writer.println("}");
        writer.close();
    }

    public void generateDepthFirstAdapterClass() throws IOException
    {
        final PrintWriter writer = new PrintWriter(new FileWriter(new File(m_targetDir, "DepthFirstAnalysisAdapter.java")));
        writer.println("package " + m_packageName + ";");
        writer.println("@SuppressWarnings(\"unused\")");
        writer.println(
            "public class DepthFirstAnalysisAdapter implements Analysis");
        writer.println("{");
        for (NodeDescriptor node : m_nodes)
        {
            String name = node.getName();
            writer.println(
                "  public void in" + name
                + "(" + name + " p_node) {}");
            writer.println(
                "  public void out" + name
                + "(" + name + " p_node) {}");
            writer.println(
                "  public void case" + name + "(" + name + " p_node)");
            writer.println("  {");
            writer.println("    in" + name + "(p_node);");
            for (NodeMember member : node.getAllMembers())
            {
                if (member.isNode())
                {
                    if (member.isList())
                    {
                        writer.println(
                            "    for (AbstractNode node : p_node."
                            + member.getGetter() + ")");
                        writer.println("    {");
                        writer.println("      node.apply(this);");
                        writer.println("    }");
                    }
                    else
                    {
                        writer.println("    p_node." + member.getGetter()
                                         + ".apply(this);");
                    }
                }
            }
            writer.println("    out" + name + "(p_node);");
            writer.println("  }");
            writer.println();
        }
        writer.println("}");
        writer.close();
    }

}
