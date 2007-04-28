package org.jamon.parser;

import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.ForNode;

public class ForParser extends AbstractFlowControlBlockParser<ForNode>
{

    public ForParser(ForNode p_node,
                     PositionalPushbackReader p_reader,
                     ParserErrorsImpl p_errors)
    {
        super(p_node, p_reader, p_errors);
    }

    @Override protected String tagName()
    {
        return "for";
    }

}
