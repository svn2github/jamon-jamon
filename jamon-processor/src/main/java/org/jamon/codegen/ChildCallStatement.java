/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

public class ChildCallStatement implements Statement {
  ChildCallStatement(int depth) {
    this.depth = depth;
  }

  @Override
  public void generateSource(CodeWriter writer, TemplateDescriber describer) {
    writer.println("child_render_" + depth + "(" + ArgNames.WRITER + ");");
  }

  private final int depth;
}
