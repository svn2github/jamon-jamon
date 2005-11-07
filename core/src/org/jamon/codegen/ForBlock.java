package org.jamon.codegen;


import org.jamon.node.Location;

public class ForBlock extends AbstractControlFlowBlock implements Statement
{
    public ForBlock(String p_condition,
                    StatementBlock p_parent,
                    Location p_conditionLocation)
    {
        super(p_parent);
        m_loopTerms = p_condition;
        m_loopTermsLocation = p_conditionLocation;
    }

    @Override protected void printOpening(CodeWriter p_writer)
    {
        p_writer.printLocation(m_loopTermsLocation);
        p_writer.println("for (" + m_loopTerms + ")");
    }

    private final String m_loopTerms;
    private final Location m_loopTermsLocation;
}
