package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserErrors;
import org.jamon.node.Location;
import org.jamon.node.NamedFragmentNode;

public class NamedFragmentParser extends AbstractBodyParser<NamedFragmentNode>
{
    public static final String NAMED_FRAGMENT_CLOSE_EXPECTED =
        "Reached end of file while inside a named call fragment; '</|>' expected";

    public NamedFragmentParser(
        NamedFragmentNode p_rootNode,
        PositionalPushbackReader p_reader,
        ParserErrors p_errors)
        throws IOException
    {
        super(p_rootNode, p_reader, p_errors);
    }

    @Override protected void handleEof()
    {
        addError(m_bodyStart, NAMED_FRAGMENT_CLOSE_EXPECTED);
    }

    @Override protected boolean handleNamedFragmentClose(Location p_tagLocation)
        throws IOException
    {
        return true;
    }
}
