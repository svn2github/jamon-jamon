package org.jamon.parser;

import org.jamon.ParserErrorsImpl;
import org.jamon.node.WhileNode;

public class WhileParser extends AbstractFlowControlBlockParser<WhileNode>
{

    public WhileParser(WhileNode p_node,
                       PositionalPushbackReader p_reader,
                       ParserErrorsImpl p_errors)
    {
        super(p_node, p_reader, p_errors);
    }

    @Override protected String tagName()
    {
        return "while";
    }

}
