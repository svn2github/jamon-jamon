package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserErrors;
import org.jamon.node.AbstractBodyNode;

public abstract class AbstractFlowControlBlockParser<Node extends AbstractBodyNode>
    extends SubcomponentParser<Node>
{
    public AbstractFlowControlBlockParser(
        Node p_node,
        PositionalPushbackReader p_reader,
        ParserErrors p_errors) throws IOException
    {
        super(p_node, p_reader, p_errors);
    }

    @Override protected void handlePostTag()
    {
    }
}
