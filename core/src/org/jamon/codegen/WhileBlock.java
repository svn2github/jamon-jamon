package org.jamon.codegen;


import org.jamon.node.Location;

public class WhileBlock extends AbstractStatementBlock implements Statement
{
    public WhileBlock(String p_condition,
                      StatementBlock p_parent,
                      Location p_conditionLocation)
    {
        super(p_parent);
        m_condition = p_condition;
        m_conditionLocation = p_conditionLocation;
    }

    @Override protected void printOpening(CodeWriter p_writer)
    {
        p_writer.printLocation(m_conditionLocation);
        p_writer.println("while (" + m_condition + ")");
    }

    private final String m_condition;
    private final Location m_conditionLocation;
}
