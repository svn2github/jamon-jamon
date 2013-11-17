/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.nodegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AnalysisGenerator {
  private final String packageName;

  private final File targetDir;

  private final Iterable<NodeDescriptor> nodes;

  public AnalysisGenerator(String packageName, File targetDir, Iterable<NodeDescriptor> nodes) {
    this.packageName = packageName;
    this.targetDir = targetDir;
    this.nodes = nodes;
  }

  public void generateAnalysisInterface() throws IOException {
    final PrintWriter writer = new PrintWriter(new FileWriter(
        new File(targetDir, "Analysis.java")));
    writer.println("package " + packageName + ";");
    writer.println("public interface Analysis {");
    for (NodeDescriptor node : nodes) {
      String name = node.getName();
      writer.println("    void case" + name + "(" + name + " node);");
    }
    writer.println("}");
    writer.close();
  }

  public void generateAnalysisAdapterClass() throws IOException {
    final PrintWriter writer = new PrintWriter(new FileWriter(new File(targetDir,
        "AnalysisAdapter.java")));
    writer.println("package " + packageName + ";");
    writer.println("public class AnalysisAdapter implements Analysis {");
    for (NodeDescriptor node : nodes) {
      String name = node.getName();
      writer.println("  @Override public void case" + name + "(" + name + " node) {}");
    }
    writer.println("}");
    writer.close();
  }

  public void generateDepthFirstAdapterClass() throws IOException {
    final PrintWriter writer = new PrintWriter(new FileWriter(new File(targetDir,
        "DepthFirstAnalysisAdapter.java")));
    writer.println("package " + packageName + ";");
    writer.println("@SuppressWarnings(\"unused\")");
    writer.println("public class DepthFirstAnalysisAdapter implements Analysis {");
    for (NodeDescriptor node : nodes) {
      String name = node.getName();
      writer.println("  public void in" + name + "(" + name + " node) {}");
      writer.println("  public void out" + name + "(" + name + " node) {}");
      writer.println("  @Override public void case" + name + "(" + name + " node) {");
      writer.println("    in" + name + "(node);");
      for (NodeMember member : node.getAllMembers()) {
        if (member.isNode()) {
          if (member.isList()) {
            String memberName = member.getName() + "Node";
            writer.println("    for (AbstractNode " + memberName + " : node." + member.getGetter() + ") {");
            writer.println("      " + memberName + ".apply(this);");
            writer.println("    }");
          }
          else {
            writer.println("    node." + member.getGetter() + ".apply(this);");
          }
        }
      }
      writer.println("    out" + name + "(node);");
      writer.println("  }");
      writer.println();
    }
    writer.println("}");
    writer.close();
  }

}
