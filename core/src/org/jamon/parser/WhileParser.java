package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserErrors;
import org.jamon.node.Location;
import org.jamon.node.WhileNode;

public class WhileParser extends SubcomponentParser
{
    public WhileParser(Location p_tagLocation,
                       String p_condition,
                       PositionalPushbackReader p_reader,
                       ParserErrors p_errors) throws IOException
    {
        super(new WhileNode(p_tagLocation, p_condition), p_reader, p_errors);
    }

    @Override protected void handlePostTag()
    {
    }

    @Override protected String tagName()
    {
        return "while";
    }
}
