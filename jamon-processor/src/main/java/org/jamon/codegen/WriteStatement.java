/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Luis O'Shea
 */

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
