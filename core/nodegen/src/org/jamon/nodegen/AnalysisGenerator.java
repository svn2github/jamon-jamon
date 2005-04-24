package org.jamon.nodegen;

import java.io.PrintWriter;
import java.util.Iterator;

public class AnalysisGenerator
{
    private AnalysisGenerator() {}
    
    public static void generateAnalysisInterface(PrintWriter p_writer,
                                             Iterator p_nodes)
    {
        p_writer.println("package org.jamon.node;");
        p_writer.println("public interface Analysis");
        p_writer.println("{");
        while (p_nodes.hasNext())
        {
            String name = ((NodeDescriptor) p_nodes.next()).getName();
            p_writer.println("    void case" + name + "(" + name + " p_node);");
        }
        p_writer.println("}");
        p_writer.close();
    }

    public static void generateAnalysisAdapterClass(PrintWriter p_writer,
                                                    Iterator p_nodes)
    {
        p_writer.println("package org.jamon.node;");
        p_writer.println("public class AnalysisAdapter implements Analysis");
        p_writer.println("{");
        while (p_nodes.hasNext())
        {
            String name = ((NodeDescriptor) p_nodes.next()).getName();
            p_writer.println(
                "  public void case" + name + "(" + name + " p_node) {}");
        }
        p_writer.println("}");
        p_writer.close();
    }

    public static void generateDepthFirstAdapterClass(PrintWriter p_writer,
                                                      Iterator p_nodes)
    {
        p_writer.println("package org.jamon.node;");
        p_writer.println("import java.util.Iterator;");
        p_writer.println(
            "public class DepthFirstAnalysisAdapter implements Analysis");
        p_writer.println("{");
        while (p_nodes.hasNext())
        {
            NodeDescriptor node = (NodeDescriptor) p_nodes.next();
            String name = node.getName();
            p_writer.println(
                "  public void in" + name + "(" + name + " p_node) {}");
            p_writer.println(
                "  public void out" + name + "(" + name + " p_node) {}");
            p_writer.println(
                "  public void case" + name + "(" + name + " p_node)");
            p_writer.println("  {");
            p_writer.println("    in" + name + "(p_node);");
            for (Iterator i = node.getAllMembers().iterator(); i.hasNext(); )
            {
                NodeMember member = (NodeMember) i.next();
                if (member.isNode())
                {
                    if (member.isList())
                    {
                        p_writer.println(
                            "    for (Iterator i = p_node." 
                            + member.getGetter() + "; i.hasNext(); )");
                        p_writer.println("    {");
                        p_writer.println(
                            "      ((AbstractNode) i.next()).apply(this);");
                        p_writer.println("    }");
                    }
                    else
                    {
                        p_writer.println("    p_node." + member.getGetter()
                                         + ".apply(this);");
                    }
                }
            }
            p_writer.println("    out" + name + "(p_node);");
            p_writer.println("  }");
            p_writer.println();
        }
        p_writer.println("}");
        p_writer.close();
    }

}
