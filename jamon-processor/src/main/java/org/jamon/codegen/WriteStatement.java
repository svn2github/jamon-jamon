/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.emit.EmitMode;

public class WriteStatement extends AbstractStatement {
  WriteStatement(String expr, EscapingDirective escapingDirective,
      org.jamon.api.Location location, String templateIdentifier, EmitMode emitMode) {
    super(location, templateIdentifier);
    this.expr = expr.trim();
    this.escapingDirective = escapingDirective;
    this.emitMode = emitMode;
  }

  @Override
  public void generateSource(CodeWriter writer, TemplateDescriber describer) {
    if (!"\"\"".equals(expr)) {
      generateSourceLine(writer);
      writer.println(escapingDirective.toJava() + ".write(" + emitMode.getEmitterClassName()
        + ".valueOf(" + expr + ")" + ", " + ArgNames.WRITER + ");");
    }
  }

  private final String expr;
  private final EscapingDirective escapingDirective;
  private final EmitMode emitMode;
}
