package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserErrors;
import org.jamon.node.ForNode;

public class ForParser extends AbstractFlowControlBlockParser<ForNode>
{

    public ForParser(ForNode p_node,
                     PositionalPushbackReader p_reader,
                     ParserErrors p_errors) throws IOException
    {
        super(p_node, p_reader, p_errors);
    }

    @Override protected String tagName()
    {
        return "for";
    }

}
