package org.jamon.parser;

import org.jamon.ParserErrorsImpl;
import org.jamon.node.UnnamedFragmentNode;

public class UnnamedFragmentParser
    extends AbstractBodyParser<UnnamedFragmentNode>
{
    public static final String FRAGMENT_CLOSE_EXPECTED =
        "Reached end of file while inside a call fragment; '</&>' expected";

    public UnnamedFragmentParser(
        UnnamedFragmentNode p_rootNode,
        PositionalPushbackReader p_reader,
        ParserErrorsImpl p_errors)
    {
        super(p_rootNode, p_reader, p_errors);
    }

    @Override protected boolean handleFragmentsClose(org.jamon.api.Location p_tagLocation)
    {
        return true;
    }

    @Override protected void handleEof()
    {
        addError(m_bodyStart, FRAGMENT_CLOSE_EXPECTED);
    }
}
