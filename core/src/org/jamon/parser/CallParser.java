package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.AbstractCallNode;
import org.jamon.node.AbstractComponentCallNode;
import org.jamon.node.AbstractParamsNode;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.ChildCallNode;
import org.jamon.node.FragmentCallNode;
import org.jamon.node.Location;
import org.jamon.node.MultiFragmentCallNode;
import org.jamon.node.NamedFragmentNode;
import org.jamon.node.NamedParamNode;
import org.jamon.node.NamedParamsNode;
import org.jamon.node.NoParamsNode;
import org.jamon.node.ParamNameNode;
import org.jamon.node.ParamValueNode;
import org.jamon.node.SimpleCallNode;
import org.jamon.node.UnnamedFragmentNode;
import org.jamon.node.UnnamedParamsNode;

/**
 * @author ian
 **/
public class CallParser extends AbstractParser
{
    public static final String INVALID_CALL_TARGET_ERROR = "Invalid call target";
    public static final String MISSING_CALL_CLOSE_ERROR = "Expecting '&>'";
    public static final String UNEXPECTED_IN_MULTI_FRAG_ERROR =
        "Expecting either '<|identifier>' or '</&>'";
    public static final String MISSING_ARG_ARROW_ERROR =
        "Expecting '=>' to separate param name and value";
    public static final String GENERIC_ERROR = "Malformed call tag";
    public static final String PARAM_VALUE_EOF_ERROR =
        "Reached end of file while reading parameter value";
    public static final String FRAGMENTS_EOF_ERROR =
        "Reached end of file while reading call fragments; '</&>' Expected";

    public CallParser(
        PositionalPushbackReader p_reader,
        ParserErrors p_errors,
        Location p_callStartLocation)
        throws IOException
    {
        super(p_reader, p_errors);
        try
        {
            if (readChar('|'))
            {
                if (readChar('|'))
                {
                    m_callNode = parseNamedFragmentCall(p_callStartLocation);
                }
                else
                {
                    m_callNode = parseUnnamedFragmentCall(p_callStartLocation);
                }
            }
            else
            {
                soakWhitespace();
                if (readChar('*'))
                {
                    Location callTargetLocation = m_reader.getLocation();
                    if (checkToken("CHILD"))
                    {
                        soakWhitespace();
                        Location endLocation = m_reader.getNextLocation();
                        if (checkToken("&>"))
                        {
                            m_callNode = new ChildCallNode(p_callStartLocation);
                        }
                        else
                        {
                            throw new ParserError(
                                endLocation, MISSING_CALL_CLOSE_ERROR);
                        }
                    }
                    else
                    {
                        throw new ParserError(
                            callTargetLocation, INVALID_CALL_TARGET_ERROR);
                    }
                }
                else
                {
                    AbstractPathNode path = parsePath();
                    m_callNode = new SimpleCallNode(
                        p_callStartLocation, path, parseParams());
                }
            }
        }
        catch (ParserError e)
        {
            addError(e);
            m_callNode =
                new SimpleCallNode(
                    p_callStartLocation,
                    new AbsolutePathNode(p_callStartLocation),
                    new NoParamsNode(m_reader.getLocation()));
        }
    }

    private AbstractComponentCallNode parseNamedFragmentCall(Location p_callStartLocation)
        throws IOException, ParserError
    {
        soakWhitespace();
        AbstractPathNode path = parsePath();
        MultiFragmentCallNode callNode =
            new MultiFragmentCallNode(p_callStartLocation, path, parseParams());
        Location fragmentsStart = m_reader.getNextLocation();
        while (true)
        {
            soakWhitespace();
            Location fragmentStart = m_reader.getNextLocation();
            int c = m_reader.read();
            if (c == '<')
            {
                switch (c = m_reader.read())
                {
                    case '|' :
                        String name = readIdentifier();
                        if (readChar('>'))
                        {
                            NamedFragmentNode fragmentNode =
                                new NamedFragmentNode(fragmentStart, name);
                            new NamedFragmentParser(
                                fragmentNode,
                                m_reader,
                                m_errors);
                            callNode.addFragment(fragmentNode);
                        }
                        else
                        {
                            throw new ParserError(
                                m_reader.getLocation(),
                                UNEXPECTED_IN_MULTI_FRAG_ERROR);
                        }
                        break;
                    case '/' :
                        if (readChar('&') && readChar('>'))
                        {
                            return callNode;
                        }
                        else
                        {
                            throw new ParserError(
                                m_reader.getLocation(),
                                UNEXPECTED_IN_MULTI_FRAG_ERROR);
                        }
                    default :
                        throw new ParserError(
                            m_reader.getLocation(),
                            UNEXPECTED_IN_MULTI_FRAG_ERROR);
                }
            }
            else if (c >= 0)
            {
                throw new ParserError(
                    m_reader.getLocation(),
                    UNEXPECTED_IN_MULTI_FRAG_ERROR);
            }
            else
            {
                throw new ParserError(fragmentsStart, FRAGMENTS_EOF_ERROR);
            }
        }
    }

    private FragmentCallNode parseUnnamedFragmentCall(Location p_callStartLocation)
        throws IOException, ParserError
    {
        soakWhitespace();
        AbstractPathNode path = parsePath();
        AbstractParamsNode params = parseParams();
        return new FragmentCallNode(
            p_callStartLocation,
            path,
            params,
            new UnamedFragmentParser(
                new UnnamedFragmentNode(m_reader.getNextLocation()),
                m_reader,
                m_errors)
                .getRootNode());
    }

    private AbstractParamsNode parseParams() throws IOException, ParserError
    {
        soakWhitespace();
        m_reader.markNodeEnd();
        int c = m_reader.read();
        switch (c)
        {
            case '&' :
                if ((c = m_reader.read()) == '>')
                {
                    return new NoParamsNode(m_reader.getCurrentNodeLocation());
                }
                else
                {
                    throw new ParserError(
                        m_reader.getLocation(),
                        GENERIC_ERROR);
                }
            case ';' :
                return parseNamedParams();
            case ':' :
                return parseUnnamedParams();
            default :
                throw new ParserError(m_reader.getLocation(), GENERIC_ERROR);
        }
    }

    private static class ParamValueEndDetector implements TagEndDetector
    {
        public boolean noMoreParams()
        {
            return m_noMoreParams;
        }

        public int checkEnd(final char p_char)
        {
            if (p_char == '&')
            {
                m_seenAmpersand = true;
                return 0;
            }
            else if (p_char == '>' && m_seenAmpersand) 
            {
                m_noMoreParams = true;
                return 2;
            }
            else if (p_char == ';')
            {
                return 1;
            }
            else
            {
                m_seenAmpersand = false;
                return 0;
            }
        }

        public ParserError getEofError(Location p_startLocation)
        {
            return new ParserError(p_startLocation, PARAM_VALUE_EOF_ERROR);
        }

        public void resetEndMatch()
        {
            m_seenAmpersand = false;
        }

        private boolean m_noMoreParams = false;
        private boolean m_seenAmpersand = false;
    }

    private NamedParamsNode parseNamedParams() throws ParserError, IOException
    {
        NamedParamsNode params = new NamedParamsNode(m_reader.getLocation());
        ParamValueEndDetector endDetector = new ParamValueEndDetector();
        while (true)
        {
            soakWhitespace();
            if (readChar('&'))
            {
                if (readChar('>'))
                {
                    return params;
                }
                else
                {
                    throw new ParserError(
                        m_reader.getCurrentNodeLocation(),
                        GENERIC_ERROR);
                }
            }
            Location nameLoc = m_reader.getNextLocation();
            String name = readIdentifier();
            readArrow();
            Location javaLoc = m_reader.getNextLocation();
            params.addParam(
                new NamedParamNode(
                    nameLoc,
                    new ParamNameNode(nameLoc, name),
                    new ParamValueNode(
                        javaLoc,
                        readJava(javaLoc, endDetector))));
            if (endDetector.noMoreParams())
            {
                return params;
            }
        }
    }

    private UnnamedParamsNode parseUnnamedParams()
        throws ParserError, IOException
    {
        UnnamedParamsNode params =
            new UnnamedParamsNode(m_reader.getLocation());
        ParamValueEndDetector endDetector = new ParamValueEndDetector();
        while (true)
        {
            soakWhitespace();
            if (readChar('&'))
            {
                if (readChar('>'))
                {
                    return params;
                }
                else
                {
                    throw new ParserError(
                        m_reader.getCurrentNodeLocation(),
                        GENERIC_ERROR);
                }
            }
            Location javaLoc = m_reader.getNextLocation();
            params.addValue(
                new ParamValueNode(javaLoc, readJava(javaLoc, endDetector)));
            if (endDetector.noMoreParams())
            {
                return params;
            }
        }
    }

    private void readArrow() throws ParserError, IOException
    {
        soakWhitespace();
        if (!(readChar('=') && readChar('>')))
        {
            throw new ParserError(
                m_reader.getNextLocation(),
                MISSING_ARG_ARROW_ERROR);
        }
        soakWhitespace();
    }

    public static void main(String[] args)
    {}

    public AbstractCallNode getCallNode()
    {
        return m_callNode;
    }

    private AbstractCallNode m_callNode;
}
