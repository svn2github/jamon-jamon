package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserErrors;
import org.jamon.node.Location;
import org.jamon.node.UnnamedFragmentNode;

public class UnnamedFragmentParser
    extends AbstractBodyParser<UnnamedFragmentNode>
{
    public static final String FRAGMENT_CLOSE_EXPECTED =
        "Reached end of file while inside a call fragment; '</&>' expected";

    public UnnamedFragmentParser(
        UnnamedFragmentNode p_rootNode,
        PositionalPushbackReader p_reader,
        ParserErrors p_errors)
        throws IOException
    {
        super(p_rootNode, p_reader, p_errors);
    }

    @Override protected boolean handleFragmentsClose(Location p_tagLocation)
        throws IOException
    {
        return true;
    }

    @Override protected void handleEof()
    {
        addError(m_bodyStart, FRAGMENT_CLOSE_EXPECTED);
    }
}
