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
