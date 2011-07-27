package org.jamon.codegen;

import org.jamon.node.ArgNode;
import org.jamon.node.FragmentArgsNode;
import org.jamon.node.OptionalArgNode;

public interface StatementBlock {
  FragmentUnit getFragmentUnitIntf(String path);

  void addStatement(Statement statement);

  FragmentUnit addFragment(FragmentArgsNode node, GenericParams genericParams);

  void addRequiredArg(ArgNode node);

  void addOptionalArg(OptionalArgNode node);

  Unit getParentUnit();

  StatementBlock getParent();
}
