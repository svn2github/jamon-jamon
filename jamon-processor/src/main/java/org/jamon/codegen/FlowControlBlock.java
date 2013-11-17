/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.codegen;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;

public class FlowControlBlock extends AbstractStatementBlock implements Statement {
  public FlowControlBlock(StatementBlock parent, String header, Location location) {
    super(parent, location);
    this.header = header;
  }

  protected void printOpening(CodeWriter writer) {
    writer.printLocation(getLocation());
    writer.println(header);
  }

  @Override
  public void generateSource(CodeWriter writer, TemplateDescriber describer)
  throws ParserErrorImpl {
    printOpening(writer);
    writer.openBlock();
    printStatements(writer, describer);
    writer.closeBlock();
  }

  private final String header;
}
