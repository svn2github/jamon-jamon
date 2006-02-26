package org.jamon.parser;

import org.jamon.ParserErrors;
import org.jamon.node.AbstractBodyNode;

public abstract class AbstractFlowControlBlockParser<Node extends AbstractBodyNode>
    extends SubcomponentParser<Node>
{
    public AbstractFlowControlBlockParser(
        Node p_node,
        PositionalPushbackReader p_reader,
        ParserErrors p_errors)
    {
        super(p_node, p_reader, p_errors);
    }

    @Override protected void handlePostTag()
    {
    }
}
