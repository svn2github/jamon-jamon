package org.jamon.codegen;

import org.jamon.ParserError;
import org.jamon.emit.EmitMode;

public abstract class AbstractControlFlowBlock extends AbstractStatementBlock
{
    public AbstractControlFlowBlock(StatementBlock p_parent)
    {
        super(p_parent);
    }

    protected abstract void printOpening(CodeWriter p_writer);

    public void generateSource(
        CodeWriter p_writer, TemplateDescriber p_describer, EmitMode p_emitMode)
        throws ParserError
    {
        printOpening(p_writer);
        p_writer.openBlock();
        printStatements(p_writer, p_describer, p_emitMode);
        p_writer.closeBlock();
    }
}
