package org.jamon.codegen;

import org.jamon.node.ArgNode;
import org.jamon.node.FragmentArgsNode;
import org.jamon.node.OptionalArgNode;

public interface StatementBlock
{
    FragmentUnit getFragmentUnitIntf(String p_path);

    void addStatement(Statement p_statement);

    FragmentUnit addFragment(
        FragmentArgsNode p_node, GenericParams p_genericParams);

    void addRequiredArg(ArgNode p_node);

    void addOptionalArg(OptionalArgNode p_node);

    Unit getParentUnit();
    StatementBlock getParent();
}