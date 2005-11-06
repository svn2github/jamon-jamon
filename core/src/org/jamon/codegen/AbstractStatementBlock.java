package org.jamon.codegen;

import java.util.LinkedList;
import java.util.List;

import org.jamon.ParserError;
import org.jamon.emit.EmitMode;
import org.jamon.node.ArgNode;
import org.jamon.node.FragmentArgsNode;
import org.jamon.node.OptionalArgNode;

public abstract class AbstractStatementBlock implements StatementBlock
{
    public AbstractStatementBlock(StatementBlock p_parent)
    {
        m_parent = p_parent;
    }

    public void generateSource(
        CodeWriter p_writer, TemplateDescriber p_describer, EmitMode p_emitMode)
        throws ParserError
    {
        printOpening(p_writer);
        p_writer.openBlock();
        printStatements(p_writer, p_describer, p_emitMode);
        p_writer.closeBlock();
    }

    protected void printStatements(
        CodeWriter p_writer, TemplateDescriber p_describer, EmitMode p_emitMode)
        throws ParserError
    {
        for (Statement statement : getStatements())
        {
            statement.generateSource(p_writer, p_describer, p_emitMode);
        }
    }

    protected abstract void printOpening(CodeWriter p_writer);

    public FragmentUnit getFragmentUnitIntf(String p_path)
    {
        return getParentUnit().getFragmentUnitIntf(p_path);
    }

    public void addStatement(Statement p_statement)
    {
        m_statements.add(p_statement);
    }

    public List<Statement> getStatements()
    {
        return m_statements;
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
