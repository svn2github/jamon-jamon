package org.jamon.parser;

import org.jamon.ParserErrors;
import org.jamon.node.ForNode;

public class ForParser extends AbstractFlowControlBlockParser<ForNode>
{

    public ForParser(ForNode p_node,
                     PositionalPushbackReader p_reader,
                     ParserErrors p_errors)
    {
        super(p_node, p_reader, p_errors);
    }

    @Override protected String tagName()
    {
        return "for";
    }

}
