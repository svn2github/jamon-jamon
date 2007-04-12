package org.jamon.codegen;

import org.jamon.ParserError;
import org.jamon.node.Location;

public class FlowControlBlock
    extends AbstractStatementBlock implements Statement
{
    public FlowControlBlock(
        StatementBlock p_parent, String p_header, Location p_location)
    {
        super(p_parent, p_location);
        m_header = p_header;
    }

    protected void printOpening(CodeWriter p_writer)
    {
        p_writer.printLocation(getLocation());
        p_writer.println(m_header);
    }

    public void generateSource(
        CodeWriter p_writer, TemplateDescriber p_describer)
        throws ParserError
    {
        printOpening(p_writer);
        p_writer.openBlock();
        printStatements(p_writer, p_describer);
        p_writer.closeBlock();
    }

    private final String m_header;
}
