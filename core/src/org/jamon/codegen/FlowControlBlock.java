package org.jamon.codegen;

import org.jamon.ParserError;
import org.jamon.emit.EmitMode;
import org.jamon.node.Location;

public class FlowControlBlock
    extends AbstractStatementBlock implements Statement
{
    public FlowControlBlock(
        StatementBlock p_parent, String p_header, Location p_location)
    {
        super(p_parent);
        m_header = p_header;
        m_location = p_location;
    }

    protected void printOpening(CodeWriter p_writer)
    {
        p_writer.printLocation(m_location);
        p_writer.println(m_header);
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

    private final String m_header;
    private final Location m_location;
}
