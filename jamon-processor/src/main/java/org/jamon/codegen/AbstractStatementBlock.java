package org.jamon.codegen;

import java.util.LinkedList;
import java.util.List;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.node.ArgNode;
import org.jamon.node.FragmentArgsNode;
import org.jamon.node.OptionalArgNode;

public abstract class AbstractStatementBlock implements StatementBlock {
  public AbstractStatementBlock(StatementBlock parent, Location location) {
    this.parent = parent;
    this.location = location;
  }

  protected void printStatements(CodeWriter writer, TemplateDescriber describer)
  throws ParserErrorImpl {
    for (Statement statement : getStatements()) {
      statement.generateSource(writer, describer);
    }
  }

  @Override
  public FragmentUnit getFragmentUnitIntf(String path) {
    return getParentUnit().getFragmentUnitIntf(path);
  }

  @Override
  public void addStatement(Statement statement) {
    if (statement instanceof LiteralStatement && !statements.isEmpty()
      && statements.get(statements.size() - 1) instanceof LiteralStatement) {
      ((LiteralStatement) statements.get(statements.size() - 1))
          .appendText(((LiteralStatement) statement).getText());
    }
    else {
      statements.add(statement);
    }
  }

  public List<Statement> getStatements() {
    return statements;
  }

  public boolean doesIO() {
    return !statements.isEmpty();
  }

  @Override
  public FragmentUnit addFragment(FragmentArgsNode node, GenericParams genericParams) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addRequiredArg(ArgNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addOptionalArg(OptionalArgNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Unit getParentUnit() {
    return parent instanceof Unit
        ? (Unit) parent
        : parent.getParentUnit();
  }

  @Override
  public StatementBlock getParent() {
    return parent;
  }

  public org.jamon.api.Location getLocation() {
    return location;
  }

  private final List<Statement> statements = new LinkedList<Statement>();

  private final StatementBlock parent;

  private final Location location;
}
