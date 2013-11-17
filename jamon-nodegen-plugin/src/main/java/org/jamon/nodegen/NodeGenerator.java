/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.nodegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class NodeGenerator {
  private final String packageName;

  public NodeGenerator(String packageName) {
    this.packageName = packageName;
  }

  private boolean containsLists(List<NodeMember> members) {
    for (NodeMember member : members) {
      if (member.isList()) {
        return true;
      }
    }
    return false;
  }

  private void writeSource(File file, NodeDescriptor node) throws IOException {
    PrintWriter writer = new PrintWriter(new FileWriter(file));
    writeHeader(writer, node.getMembers());
    writeConstructor(writer, node);
    writeApply(writer, node);
    writeMembers(writer, node.getName(), node.getMembers());
    writeEquals(writer, node.getName(), node.getMembers());
    writeHashCode(writer, node.getMembers());
    writeToString(writer, node.getMembers());
    writer.println("}");
    writer.close();
  }

  private void writeApply(PrintWriter writer, NodeDescriptor node) {
    writer.println("  @Override public void apply(Analysis analysis)");
    writer.println("  {");
    writer.println("    analysis.case" + node.getName() + "(this);");
    writer.println("  }");
    writer.println();
  }

  private void writeHeader(PrintWriter writer, List<NodeMember> members) {
    writer.println("package " + packageName + ";");
    writer.println();
    if (containsLists(members)) {
      writer.println("import java.util.ArrayList;");
      writer.println("import java.util.List;");
      writer.println();
    }
    writer.println("import org.jamon.api.Location;");
  }

  private void writeConstructor(PrintWriter writer, NodeDescriptor node) {
    writer.println("public class " + node.getName() + " extends " + node.getParent() + " {");
    writer.print("  public " + node.getName() + "(Location location");
    for (NodeMember member : node.getParentMembers()) {
      if (!member.isList()) {
        writer.print(", " + member.getType() + " " + member.getName());
      }
    }
    for (NodeMember member : node.getMembers()) {
      if (!member.isList()) {
        writer.print(", " + member.getType() + " " + member.getName());
      }
    }
    writer.println(") {");
    writer.print("    super(location");
    for (NodeMember member : node.getParentMembers()) {
      if (!member.isList()) {
        writer.print(", " + member.getName());
      }
    }
    writer.println(");");
    for (NodeMember member : node.getMembers()) {
      if (!member.isList()) {
        if (!member.isPrimative()) {
          writer.println("    if ((this." + member.instanceName() + " = " + member.getName()
            + ") == null)");
          writer.println("      { throw new NullPointerException(); }");
        }
        else {
          writer.println("    this." + member.instanceName() + " = " + member.getName() + ";");
        }
      }
    }
    writer.println("  }");
    writer.println();
  }

  private void writeMembers(PrintWriter writer, String nodeName, List<NodeMember> members) {
    for (NodeMember member : members) {
      if (member.isList()) {
        writer.println("  private final List<" + member.getType() + "> " + member.instanceName()
          + " = new ArrayList<" + member.getType() + ">();");
        writer.println("  public " + nodeName + " add" + member.getCapitalizedName() + "("
          + member.getType() + " " + member.parameterName() + ") {");
        writer.println("    if (" + member.parameterName()
          + " == null) { throw new NullPointerException(); }");
        writer.println("    " + member.instanceName() + ".add(" + member.parameterName() + ");");
        writer.println("    return this;");
        writer.println("  }");
        writer.println("  public List<" + member.getType() + "> " + member.getGetter()
          + " { return " + member.instanceName() + "; }");
      }
      else {
        writer.println("  private final " + member.getType() + " " + member.instanceName() + ";");
        writer.println("  public " + member.getType() + " " + member.getGetter() + " { return "
          + member.instanceName() + "; }");
      }
      writer.println();
    }
  }

  private void writeEquals(PrintWriter writer, String nodeName, List<NodeMember> members) {
    writer.println("  @Override public boolean equals(Object obj) {");
    writer.println("    return obj != null");
    writer.println("        && super.equals(obj)");
    for (NodeMember member : members) {
      writer.print("      && " + member.instanceName());
      if (member.isPrimative()) {
        writer.print(" == ((");
      }
      else {
        writer.print(".equals(((");
      }
      writer.print(nodeName + ") obj)." + member.instanceName());
      if (member.isPrimative()) {
        writer.println();
      }
      else {
        writer.println(")");
      }
    }
    writer.println("    ;");
    writer.println("  }");
    writer.println();
  }

  private void writeHashCode(PrintWriter writer, List<NodeMember> members) {
    writer.println("  @Override public int hashCode() {");
    writer.println("    return super.hashCode()");
    for (NodeMember member : members) {
      writer.println("      ^ " + member.hashCodeExpr());
    }
    writer.println("    ;");
    writer.println("  }");
    writer.println();
  }

  private void writeToString(PrintWriter writer, List<NodeMember> members) {
    writer.println("  @Override protected void propertiesToString(StringBuilder buffer) {");
    writer.println("    super.propertiesToString(buffer);");
    for (NodeMember member : members) {
      writer.print("    addProperty");
      if (member.isList()) {
        writer.print("List");
      }
      writer.print("(buffer, \"" + member.getName() + "\", " + member.instanceName());
      writer.println(");");

    }
    writer.println("  }");
    writer.println();
  }

  /**
   * Create Java source files for nodes described in a node file.
   *
   * @param nodes A description of the nodes to generate Java sources for.
   * @param sourceDir The directory to place generated files in.
   * @throws IOException
   **/
  public void generateSources(Iterable<NodeDescriptor> nodes, File sourceDir) throws IOException {
    for (NodeDescriptor node : nodes) {
      writeSource(new File(sourceDir, node.getName() + ".java"), node);
    }
  }
}
