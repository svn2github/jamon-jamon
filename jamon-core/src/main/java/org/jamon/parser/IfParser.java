package org.jamon.parser;

import java.io.IOException;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractBodyNode;
import org.jamon.node.ElseIfNode;
import org.jamon.node.ElseNode;

public class IfParser extends AbstractFlowControlBlockParser<AbstractBodyNode>
{
    public static final String ENCOUNTERED_MULTIPLE_ELSE_TAGS_FOR_ONE_IF_TAG =
        "encountered multiple <%else> tags for one <%if ...%> tag";

    public IfParser(AbstractBodyNode p_node,
                    PositionalPushbackReader p_reader,
                    ParserErrorsImpl p_errors)
    {
        super(p_node, p_reader, p_errors);
    }

    @Override protected void handleElseTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (processingElseNode())
        {
            addError(
                p_tagLocation, ENCOUNTERED_MULTIPLE_ELSE_TAGS_FOR_ONE_IF_TAG);
        }
        else
        {
            if (checkForTagClosure(p_tagLocation))
            {
                m_continuation = new IfParser(
                    new ElseNode(p_tagLocation), m_reader, m_errors);
                m_continuation.parse();
            }
            doneParsing();
        }
    }

    @Override protected void handleElseIfTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (processingElseNode())
        {
            addError(
                p_tagLocation, ENCOUNTERED_MULTIPLE_ELSE_TAGS_FOR_ONE_IF_TAG);
        }
        else
        {
            try
            {
                m_continuation = new IfParser(
                    new ElseIfNode(
                        p_tagLocation, readCondition(p_tagLocation, "elseif")),
                    m_reader,
                    m_errors);
                m_continuation.parse();
            }
            catch (ParserErrorImpl e)
            {
                addError(e);
            }
            doneParsing();
        }
    }

    private boolean processingElseNode()
    {
        return m_root instanceof ElseNode;
    }

    public IfParser getContinuation()
    {
        return m_continuation;
    }

    private IfParser m_continuation;

    @Override protected String tagName()
    {
        return "if";
    }
}
