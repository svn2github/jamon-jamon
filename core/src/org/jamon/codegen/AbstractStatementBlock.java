package org.jamon.codegen;

import java.util.LinkedList;
import java.util.List;

import org.jamon.ParserError;
import org.jamon.node.ArgNode;
import org.jamon.node.FragmentArgsNode;
import org.jamon.node.OptionalArgNode;

public abstract class AbstractStatementBlock implements StatementBlock
{
    public AbstractStatementBlock(StatementBlock p_parent)
    {
        m_parent = p_parent;
    }

    protected void printStatements(
        CodeWriter p_writer, TemplateDescriber p_describer)
        throws ParserError
    {
        for (Statement statement : getStatements())
        {
            statement.generateSource(p_writer, p_describer);
        }
    }

    public FragmentUnit getFragmentUnitIntf(String p_path)
    {
        return getParentUnit().getFragmentUnitIntf(p_path);
    }

    public void addStatement(Statement p_statement)
    {
        if (p_statement instanceof LiteralStatement
            && !m_statements.isEmpty()
            && m_statements.get(m_statements.size() - 1)
                instanceof LiteralStatement)
        {
            ((LiteralStatement) m_statements.get(m_statements.size() - 1))
                .appendText(((LiteralStatement) p_statement).getText());
        }
        else
        {
            m_statements.add(p_statement);
        }
    }

    public List<Statement> getStatements()
    {
        return m_statements;
    }

    public boolean canThrowIOException()
    {
        return ! m_statements.isEmpty();
    }

    public FragmentUnit addFragment(FragmentArgsNode p_node, GenericParams p_genericParams)
    {
        throw new UnsupportedOperationException();
    }

    public void addRequiredArg(ArgNode p_node)
    {
        throw new UnsupportedOperationException();
    }

    public void addOptionalArg(OptionalArgNode p_node)
    {
        throw new UnsupportedOperationException();
    }

    public Unit getParentUnit()
    {
        return m_parent instanceof Unit
            ? (Unit) m_parent
            : m_parent.getParentUnit();
    }

    public StatementBlock getParent()
    {
        return m_parent;
    }

    private final List<Statement> m_statements = new LinkedList<Statement>();
    private final StatementBlock m_parent;
}
