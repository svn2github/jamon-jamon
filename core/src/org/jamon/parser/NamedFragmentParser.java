package org.jamon.parser;

import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.NamedFragmentNode;

public class NamedFragmentParser extends AbstractBodyParser<NamedFragmentNode>
{
    public static final String NAMED_FRAGMENT_CLOSE_EXPECTED =
        "Reached end of file while inside a named call fragment; '</|>' expected";

    public NamedFragmentParser(
        NamedFragmentNode p_rootNode,
        PositionalPushbackReader p_reader,
        ParserErrorsImpl p_errors)
    {
        super(p_rootNode, p_reader, p_errors);
    }

    @Override protected void handleEof()
    {
        addError(m_bodyStart, NAMED_FRAGMENT_CLOSE_EXPECTED);
    }

    @Override protected boolean handleNamedFragmentClose(org.jamon.api.Location p_tagLocation)
    {
        return true;
    }
}
