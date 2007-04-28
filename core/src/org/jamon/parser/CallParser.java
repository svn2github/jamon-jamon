/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.AbstractCallNode;
import org.jamon.node.AbstractComponentCallNode;
import org.jamon.node.AbstractParamsNode;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.ChildCallNode;
import org.jamon.node.FragmentCallNode;
import org.jamon.node.GenericCallParam;
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

public class CallParser extends AbstractParser
{
    public static final String INVALID_CALL_TARGET_ERROR = "Invalid call target";
    public static final String MISSING_CALL_CLOSE_ERROR = "Expecting '&>'";
    public static final String UNEXPECTED_IN_MULTI_FRAG_ERROR =
        "Expecting either '<|identifier>' or '</&>'";
    public static final String MISSING_ARG_ARROW_ERROR =
        "Expecting '=' or '=>' to separate param name and value";
    public static final String GENERIC_ERROR = "Malformed call tag";
    public static final String PARAM_VALUE_EOF_ERROR =
        "Reached end of file while reading parameter value";
    public static final String FRAGMENTS_EOF_ERROR =
        "Reached end of file while reading call fragments; '</&>' Expected";
    public static final String MISSING_GENERIC_PARAM_CLOSE_ERROR =
        "Expecing ',' or '>'";

    public CallParser(
        PositionalPushbackReader p_reader,
        ParserErrorsImpl p_errors,
        org.jamon.api.Location p_callStartLocation)
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
                addGenericParams();
            }
            else
            {
                soakWhitespace();
                if (readChar('*'))
                {
                    parseChildCall(p_callStartLocation);
                }
                else
                {
                    AbstractPathNode path = parsePath();
                    parseGenericParams();
                    m_callNode = new SimpleCallNode(
                        p_callStartLocation, path, parseParams());
                    addGenericParams();
                }
            }
        }
        catch (ParserErrorImpl e)
        {
            addError(e);
            m_callNode =
                new SimpleCallNode(
                    p_callStartLocation,
                    new AbsolutePathNode(p_callStartLocation),
                    new NoParamsNode(m_reader.getLocation()));
        }
    }

    private void parseChildCall(org.jamon.api.Location p_callStartLocation)
        throws IOException, ParserErrorImpl
    {
        org.jamon.api.Location callTargetLocation = m_reader.getLocation();
        if (checkToken("CHILD"))
        {
            soakWhitespace();
            org.jamon.api.Location endLocation = m_reader.getNextLocation();
            if (checkToken("&>"))
            {
                m_callNode = new ChildCallNode(p_callStartLocation);
            }
            else
            {
                throw new ParserErrorImpl(
                    endLocation, MISSING_CALL_CLOSE_ERROR);
            }
        }
        else
        {
            throw new ParserErrorImpl(
                callTargetLocation, INVALID_CALL_TARGET_ERROR);
        }
    }

    private AbstractComponentCallNode parseNamedFragmentCall(
        org.jamon.api.Location p_callStartLocation)
        throws IOException, ParserErrorImpl
    {
        soakWhitespace();
        AbstractPathNode path = parsePath();
        parseGenericParams();
        MultiFragmentCallNode callNode =
            new MultiFragmentCallNode(p_callStartLocation, path, parseParams());
        org.jamon.api.Location fragmentsStart = m_reader.getNextLocation();
        while (true)
        {
            soakWhitespace();
            org.jamon.api.Location fragmentStart = m_reader.getNextLocation();
            int c = m_reader.read();
            if (c == '<')
            {
                switch (c = m_reader.read())
                {
                    case '|' :
                        String name = readIdentifier(true);
                        if (readChar('>'))
                        {
                            NamedFragmentNode fragmentNode =
                                new NamedFragmentNode(fragmentStart, name);
                            new NamedFragmentParser(
                                fragmentNode,
                                m_reader,
                                m_errors)
                                .parse();
                            callNode.addFragment(fragmentNode);
                        }
                        else
                        {
                            throw new ParserErrorImpl(
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
                            throw new ParserErrorImpl(
                                m_reader.getLocation(),
                                UNEXPECTED_IN_MULTI_FRAG_ERROR);
                        }
                    default :
                        throw new ParserErrorImpl(
                            m_reader.getLocation(),
                            UNEXPECTED_IN_MULTI_FRAG_ERROR);
                }
            }
            else if (c >= 0)
            {
                throw new ParserErrorImpl(
                    m_reader.getLocation(),
                    UNEXPECTED_IN_MULTI_FRAG_ERROR);
            }
            else
            {
                throw new ParserErrorImpl(fragmentsStart, FRAGMENTS_EOF_ERROR);
            }
        }
    }

    private FragmentCallNode parseUnnamedFragmentCall(org.jamon.api.Location p_callStartLocation)
        throws IOException, ParserErrorImpl
    {
        soakWhitespace();
        AbstractPathNode path = parsePath();
        parseGenericParams();
        AbstractParamsNode params = parseParams();
        return new FragmentCallNode(
            p_callStartLocation,
            path,
            params,
            new UnnamedFragmentParser(
            new UnnamedFragmentNode(m_reader.getNextLocation()),
            m_reader,
            m_errors)
            .parse()
            .getRootNode());
    }

    private AbstractParamsNode parseParams() throws IOException, ParserErrorImpl
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
                    throw new ParserErrorImpl(
                        m_reader.getLocation(),
                        GENERIC_ERROR);
                }
            case ';' :
                return parseNamedParams();
            case ':' :
                return parseUnnamedParams();
            default :
                throw new ParserErrorImpl(m_reader.getLocation(), GENERIC_ERROR);
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

        public ParserErrorImpl getEofError(org.jamon.api.Location p_startLocation)
        {
            return new ParserErrorImpl(p_startLocation, PARAM_VALUE_EOF_ERROR);
        }

        public void resetEndMatch()
        {
            m_seenAmpersand = false;
        }

        private boolean m_noMoreParams = false;
        private boolean m_seenAmpersand = false;
    }

    private NamedParamsNode parseNamedParams() throws ParserErrorImpl, IOException
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
                    throw new ParserErrorImpl(
                        m_reader.getCurrentNodeLocation(),
                        GENERIC_ERROR);
                }
            }
            org.jamon.api.Location nameLoc = m_reader.getNextLocation();
            String name = readIdentifier(true);
            readArrow();
            org.jamon.api.Location javaLoc = m_reader.getNextLocation();
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
        throws ParserErrorImpl, IOException
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
                    throw new ParserErrorImpl(
                        m_reader.getCurrentNodeLocation(),
                        GENERIC_ERROR);
                }
            }
            org.jamon.api.Location javaLoc = m_reader.getNextLocation();
            params.addValue(
                new ParamValueNode(javaLoc, readJava(javaLoc, endDetector)));
            if (endDetector.noMoreParams())
            {
                return params;
            }
        }
    }

    private void parseGenericParams() throws ParserErrorImpl, IOException
    {
        m_genericParams = new ArrayList<GenericCallParam>();
        if (readChar('<'))
        {
            do
            {
               soakWhitespace();
               org.jamon.api.Location location = m_reader.getNextLocation();
               m_genericParams.add(
                   new GenericCallParam(
                       location,
                       new ClassNameParser(location, m_reader, m_errors)
                       .getType()));
               soakWhitespace();
            }
            while(readChar(','));
            if (!readChar('>'))
            {
                throw new ParserErrorImpl(m_reader.getNextLocation(),
                                      MISSING_GENERIC_PARAM_CLOSE_ERROR);
            }
        }
    }

    private void addGenericParams()
    {
        AbstractComponentCallNode callNode =
            (AbstractComponentCallNode) m_callNode;
        for (GenericCallParam param : m_genericParams)
        {
            callNode.addGenericParam(param);
        }
    }

    private void readArrow() throws ParserErrorImpl, IOException
    {
        soakWhitespace();
        if (!readChar('='))
        {
            throw new ParserErrorImpl(
                m_reader.getNextLocation(),
                MISSING_ARG_ARROW_ERROR);
        }
        readChar('>'); // support old-style syntax
        soakWhitespace();
    }

    public static void main(String[] args)
    {}

    public AbstractCallNode getCallNode()
    {
        return m_callNode;
    }

    private AbstractCallNode m_callNode;
    private List<GenericCallParam> m_genericParams = null;
}
