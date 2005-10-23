package org.jamon.nodegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class NodeGenerator
{
    private NodeGenerator()
    {}
    private static boolean containsLists(List<NodeMember> p_members)
    {
        for (NodeMember member : p_members)
        {
            if (member.isList())
            {
                return true;
            }
        }
        return false;
    }

    private static void writeSource(File p_file, NodeDescriptor p_node)
        throws IOException
    {
        PrintWriter writer = new PrintWriter(new FileWriter(p_file));
        writeHeader(writer, p_node.getMembers());
        writeConstructor(writer, p_node);
        writeApply(writer, p_node);
        writeMembers(writer, p_node.getName(), p_node.getMembers());
        writeEquals(writer, p_node.getName(), p_node.getMembers());
        writeHashCode(writer, p_node.getMembers());
        writeToString(writer, p_node.getMembers());
        writer.println("}");
        writer.close();
    }

    private static void writeApply(PrintWriter p_writer, NodeDescriptor p_node)
    {
        p_writer.println("  @Override public void apply(Analysis p_analysis)");
        p_writer.println("  {");
        p_writer.println("    p_analysis.case" + p_node.getName() + "(this);");
        p_writer.println("  }");
        p_writer.println();
    }

    private static void writeHeader(
        PrintWriter p_writer, List<NodeMember> p_members)
    {
        p_writer.println("package org.jamon.node;");
        p_writer.println();
        if (containsLists(p_members))
        {
            p_writer.println("import java.util.ArrayList;");
            p_writer.println("import java.util.List;");
            p_writer.println();
        }
    }

    private static void writeConstructor(
        PrintWriter p_writer, NodeDescriptor p_node)
    {
        p_writer.println(
           "public class " + p_node.getName()
           + " extends " + p_node.getParent());
        p_writer.println("{");
        p_writer.print("  public " + p_node.getName() + "(Location p_location");
        for (NodeMember member : p_node.getParentMembers())
        {
            if (!member.isList())
            {
                p_writer.print(", " + member.getType()
                               + " p_" + member.getName());
            }
        }
        for (NodeMember member : p_node.getMembers())
        {
            if (!member.isList())
            {
                p_writer.print(", " + member.getType() + " p_" + member.getName());
            }
        }
        p_writer.println(")");
        p_writer.println("  {");
        p_writer.print("    super(p_location");
        for (NodeMember member : p_node.getParentMembers())
        {
            if (!member.isList())
            {
                p_writer.print(", p_" + member.getName());
            }
        }
        p_writer.println(");");
        for (NodeMember member : p_node.getMembers())
        {
            if (!member.isList())
            {
                if (!member.isPrimative())
                {
                    p_writer.println(
                        "    if (("
                            + member.instanceName()
                            + " = p_"
                            + member.getName()
                            + ") == null)");
                    p_writer.println(
                        "      { throw new NullPointerException(); }");
                }
                else
                {
                    p_writer.println(
                        "    "
                            + member.instanceName()
                            + " = p_"
                            + member.getName()
                            + ";");
                }
            }
        }
        p_writer.println("  }");
        p_writer.println();
    }

    private static void writeMembers(
        PrintWriter p_writer,
        String p_nodeName,
        List<NodeMember> p_members)
    {
        for (NodeMember member : p_members)
        {
            if (member.isList())
            {
                p_writer.println(
                    "  private final List<" + member.getType() + "> "
                        + member.instanceName()
                        + " = new ArrayList<" + member.getType() + ">();");
                p_writer.println(
                    "  public "
                        + p_nodeName
                        + " add"
                        + member.getCapitalizedName()
                        + "("
                        + member.getType()
                        + " p_"
                        + member.getName()
                        + ")");
                p_writer.println("  {");
                p_writer.println(
                    "    if (p_"
                        + member.getName()
                        + " == null) { throw new NullPointerException(); }");
                p_writer.println(
                    "    "
                        + member.instanceName()
                        + ".add(p_"
                        + member.getName()
                        + ");");
                p_writer.println("    return this;");
                p_writer.println("  }");
                p_writer.println(
                    "  public List<" + member.getType() + "> "
                    + member.getGetter()
                    + " { return " + member.instanceName() + "; }");
            }
            else
            {
                p_writer.println(
                    "  private final "
                        + member.getType()
                        + " "
                        + member.instanceName()
                        + ";");
                p_writer.println(
                    "  public "
                        + member.getType() + " "
                        + member.getGetter()
                        + " { return " + member.instanceName() + "; }");
            }
            p_writer.println();
        }
    }

    private static void writeEquals(
        PrintWriter p_writer,
        String p_nodeName,
        List<NodeMember> p_members)
    {
        p_writer.println("  @Override public boolean equals(Object p_obj)");
        p_writer.println("  {");
        p_writer.println("    return super.equals(p_obj)");
        for (NodeMember member : p_members)
        {
            p_writer.print("      && " + member.instanceName());
            if (member.isPrimative())
            {
                p_writer.print(" == ((");
            }
            else
            {
                p_writer.print(".equals(((");
            }
            p_writer.print(p_nodeName + ") p_obj)." + member.instanceName());
            if (member.isPrimative())
            {
                p_writer.println();
            }
            else
            {
                p_writer.println(")");
            }
        }
        p_writer.println("    ;");
        p_writer.println("  }");
        p_writer.println();
    }

    private static void writeHashCode(
        PrintWriter p_writer, List<NodeMember> p_members)
    {
        p_writer.println("  @Override public int hashCode()");
        p_writer.println("  {");
        p_writer.println("    return super.hashCode()");
        for (NodeMember member : p_members)
        {
            p_writer.println("      ^ " + member.hashCodeExpr());
        }
        p_writer.println("    ;");
        p_writer.println("  }");
        p_writer.println();
    }

    private static void writeToString(
        PrintWriter p_writer, List<NodeMember> p_members)
    {
        p_writer.println(
            "  @Override protected void propertiesToString(StringBuilder p_buffer)");
        p_writer.println("  {");
        p_writer.println("    super.propertiesToString(p_buffer);");
        for (NodeMember member : p_members)
        {
            p_writer.print("    addProperty");
            if (member.isList())
            {
                p_writer.print("List");
            }
            p_writer.print(
                "(p_buffer, \"" + member.getName() + "\", " + member.instanceName());
            if (member.isList())
            {
                p_writer.print(".iterator()");
            }
            p_writer.println(");");

        }
        p_writer.println("  }");
        p_writer.println();
    }

    /**
     * Create Java source files for nodes described in a node file.
     * @param p_nodesDescriptor A description of the nodes
     *         to generate Java sources for.
     * @param p_sourceDir The directory to place generated files in.
     * @throws IOException
     **/
    public static void generateSources(
        Iterable<NodeDescriptor> p_nodes, File p_sourceDir)
        throws IOException
    {
        for (NodeDescriptor node : p_nodes)
        {
            writeSource(new File(p_sourceDir, node.getName() + ".java"), node);
        }
    }
}
