package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserErrors;
import org.jamon.node.AbstractBodyNode;

public abstract class AbstractFlowControlBlockParser extends SubcomponentParser
{
    public AbstractFlowControlBlockParser(
        AbstractBodyNode p_node,
        PositionalPushbackReader p_reader,
        ParserErrors p_errors) throws IOException
    {
        super(p_node, p_reader, p_errors);
    }

    @Override protected void handlePostTag()
    {
    }
}
