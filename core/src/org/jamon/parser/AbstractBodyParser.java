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

import org.jamon.ParserErrorImpl;
import org.jamon.ParserErrorsImpl;
import org.jamon.node.AbstractBodyNode;
import org.jamon.node.DefaultEscapeNode;
import org.jamon.node.DocNode;
import org.jamon.node.EmitNode;
import org.jamon.node.EscapeNode;
import org.jamon.node.ForNode;
import org.jamon.node.IfNode;
import org.jamon.node.JavaNode;
import org.jamon.node.LiteralNode;
import org.jamon.node.TextNode;
import org.jamon.node.WhileNode;

/**
 * @author ian
 **/

public abstract class AbstractBodyParser<Node extends AbstractBodyNode>
    extends AbstractParser
{
    public static final String ENCOUNTERED_ELSE_TAG_WITHOUT_PRIOR_IF_TAG =
        "encountered <%else> tag without prior <%if ...%> tag";
    public static final String ENCOUNTERED_ELSEIF_TAG_WITHOUT_PRIOR_IF_TAG =
        "encountered <%elseif ...%> tag without prior <%if ...%> tag";
    public static final String ESCAPE_TAG_IN_SUBCOMPONENT =
        "<%escape> tags only allowed at the top level of a document";
    public static final String GENERIC_TAG_IN_SUBCOMPONENT =
        "<%generic> tags only allowed at the top level of a document";
    public static final String ANNOTATE_TAG_IN_SUBCOMPONENT =
        "<%annotate> tags only allowed at the top level of a document";
    public static final String CLASS_TAG_IN_SUBCOMPONENT =
        "<%class> sections only allowed at the top level of a document";
    public static final String UNEXPECTED_NAMED_FRAGMENT_CLOSE_ERROR =
        "</|> tags can only be used to close named call fragments";
    public static final String UNEXPECTED_FRAGMENTS_CLOSE_ERROR =
        "</&> tags can only be used to close call fragments";
    public static final String EMIT_ESCAPE_CODE_ERROR =
        "Emit escaping code must be a letter";
    public static final String EMIT_MISSING_TAG_END_ERROR =
        "Did not see expected '%>' to end a <% ... %> tag";
    public static final String PERCENT_GREATER_THAN_EOF_ERROR =
        "Reached end of file while looking for '%>'";
    public static final String EXTENDS_TAG_IN_SUBCOMPONENT =
        "<%extends ...> tag only allowed at the top level of a document";
    private static final String ALIASES_TAG_IN_SUBCOMPONENT =
        "<%aliases> sections only allowed at the top level of a document";
    public static final String IMPLEMENTS_TAG_IN_SUBCOMPONENT =
        "<%implements> sections only allowed at the top level of a document";
    private static final String IMPORT_TAG_IN_SUBCOMPONENT =
        "<%import> sections only allowed at the top level of a document";
    public static final String PARENT_ARGS_TAG_IN_SUBCOMPONENT =
        "<%xargs> sections only allowed at the top level of a document";
    public static final String PARENT_MARKER_TAG_IN_SUBCOMPONENT =
        "<%abstract> tag only allowed at the top level of a document";

    protected AbstractBodyParser(
        Node p_rootNode,
        PositionalPushbackReader p_reader,
        ParserErrorsImpl p_errors)
    {
        super(p_reader, p_errors);
        m_root = p_rootNode;
        m_bodyStart = m_reader.getNextLocation();
    }

    protected void handleText()
    {
        if (m_text.length() > 0)
        {
            m_root.addSubNode(
                new TextNode(
                    m_reader.getCurrentNodeLocation(),
                    m_text.toString()));
            m_text = new StringBuilder();
        }
        m_reader.markNodeBeginning();
    }

    public AbstractBodyParser<Node> parse() throws IOException
    {
        int c;
        m_doneParsing = false;
        m_reader.markNodeEnd();
        boolean isTopLevel = isTopLevel();
        while ((isTopLevel || !m_doneParsing) && (c = m_reader.read()) >= 0)
        {
            if (c == '<')
            {
                org.jamon.api.Location tagLocation = m_reader.getLocation();
                int c1 = m_reader.read();
                switch (c1)
                {
                    case '%' : // a tag, emit or java snippet
                        handleText();
                        if (soakWhitespace())
                        {
                            handleEmit(tagLocation);
                        }
                        else
                        {
                            handleTag(readTagName(), tagLocation);
                        }
                        m_reader.markNodeEnd();
                        break;
                    case '&' :
                        handleText();
                        m_root.addSubNode(
                            new CallParser(m_reader, m_errors, tagLocation)
                                .getCallNode());
                        m_reader.markNodeEnd();
                        break;
                    case '/' :
                        switch (c = m_reader.read())
                        {
                            case '%' :
                                String tagName = readTagName();
                                doneParsing();
                                if (checkForTagClosure(tagLocation))
                                {
                                    handleTagClose(tagName, tagLocation);
                                }
                                break;
                            case '&' :
                                if (readChar('>'))
                                {
                                    if(handleFragmentsClose(tagLocation))
                                    {
                                        doneParsing();
                                    }
                                }
                                else
                                {
                                    m_text.append("</&");
                                }
                                break;
                            case '|' :
                                if (readChar('>'))
                                {
                                    if (handleNamedFragmentClose(tagLocation))
                                    {
                                        doneParsing();
                                    }
                                }
                                else
                                {
                                    m_text.append("</|");
                                }
                                break;
                            default :
                                m_reader.unread(c);
                                m_text.append("</");
                        }
                        break;
                    default :
                        if (c1 >= 0)
                        {
                            m_reader.unread(c1);
                        }
                        m_text.append((char) c);
                        break;
                }
            }
            else if (c == '%' && m_reader.isLineStart())
            {
                handleText();
                m_root.addSubNode(
                    new JavaNode(
                        m_reader.getCurrentNodeLocation(),
                        readLine()));
                m_reader.markNodeEnd();
            }
            else if (c == '\\')
            {
                int c1 = m_reader.read();
                if (c1 != '\n')
                {
                    m_text.append((char) c);
                    m_reader.unread(c1);
                }
            }
            else
            {
                m_text.append((char) c);
            }
        }
        handleText();
        if (!(m_doneParsing || isTopLevel))
        {
            handleEof();
        }
        return this;
    }

    protected void doneParsing()
    {
        m_doneParsing = true;
    }

    /**
     * @param tagLocation Start of the emit.
     **/
    private void handleEmit(org.jamon.api.Location p_tagLocation) throws IOException
    {
        try
        {
            HashEndDetector endDetector = new HashEndDetector();
            String emitExpr = readJava(p_tagLocation, endDetector);
            if (! endDetector.endedWithHash())
            {
                m_root.addSubNode(
                    new EmitNode(
                        p_tagLocation,
                        emitExpr,
                        new DefaultEscapeNode(m_reader.getLocation())));
            }
            else
            {
                org.jamon.api.Location escapingLocation = m_reader.getLocation();
                int c = m_reader.read();
                if (isLetter((char) c))
                {
                    soakWhitespace();
                    if (readChar('%') && readChar('>'))
                    {
                        m_root.addSubNode(
                            new EmitNode(
                                p_tagLocation,
                                emitExpr,
                                new EscapeNode(escapingLocation,
                                               Character.toString((char) c))));
                    }
                    else
                    {
                        addError(
                            m_reader.getLocation(),
                            EMIT_MISSING_TAG_END_ERROR);
                    }
                }
                else
                {
                    addError(m_reader.getLocation(), EMIT_ESCAPE_CODE_ERROR);
                }
            }
        }
        catch (ParserErrorImpl e)
        {
            addError(e);
        }
    }

    private boolean isLetter(char p_char)
    {
        return ('A' <= p_char && p_char <= 'Z')
            || ('a' <= p_char && p_char <= 'z');
    }

    /**
     * @return <code>true</code> if this is a top level parser
     **/
    protected boolean isTopLevel()
    {
        return false;
    }

    protected void handleTag(
        final String p_tagName,
        final org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if ("java".equals(p_tagName))
        {
            handleJavaTag(p_tagLocation);
        }
        else if ("LITERAL".equals(p_tagName))
        {
            handleLiteralTag(p_tagLocation);
        }
        else if ("def".equals(p_tagName))
        {
            handleDefTag(p_tagLocation);
        }
        else if ("method".equals(p_tagName))
        {
            handleMethodTag(p_tagLocation);
        }
        else if ("override".equals(p_tagName))
        {
            handleOverrideTag(p_tagLocation);
        }
        else if ("while".equals(p_tagName))
        {
           handleWhileTag(p_tagLocation);
        }
        else if ("for".equals(p_tagName))
        {
            handleForTag(p_tagLocation);
        }
        else if ("if".equals(p_tagName))
        {
            handleIfTag(p_tagLocation);
        }
        else if ("else".equals(p_tagName))
        {
            handleElseTag(p_tagLocation);
        }
        else if ("elseif".equals(p_tagName))
        {
            handleElseIfTag(p_tagLocation);
        }
        else if ("args".equals(p_tagName))
        {
            //FIXME - either all handlers should throw these, or none.
            try
            {
                m_root.addSubNode(
                    new ArgsParser(m_reader, m_errors, p_tagLocation)
                        .getArgsNode());
            }
            catch (ParserErrorImpl e)
            {
                addError(e);
            }
        }
        else if ("frag".equals(p_tagName))
        {
            try
            {
                m_root.addSubNode(
                    new FragmentArgsParser(m_reader, m_errors, p_tagLocation)
                        .getFragmentArgsNode());
            }
            catch (ParserErrorImpl e)
            {
                addError(e);
            }
        }
        else if ("xargs".equals(p_tagName))
        {
            handleParentArgsNode(p_tagLocation);
        }
        else if ("class".equals(p_tagName))
        {
            handleClassTag(p_tagLocation);
        }
        else if ("extends".equals(p_tagName))
        {
            handleExtendsTag(p_tagLocation);
        }
        else if ("alias".equals(p_tagName))
        {
            handleAliasesTag(p_tagLocation);
        }
        else if ("absmeth".equals(p_tagName))
        {
            handleAbsMethodTag(p_tagLocation);
        }
        else if ("implements".equals(p_tagName))
        {
            handleImplementsTag(p_tagLocation);
        }
        else if ("import".equals(p_tagName))
        {
            handleImportTag(p_tagLocation);
        }
        else if ("doc".equals(p_tagName))
        {
            handleDocTag(p_tagLocation);
        }
        else if ("abstract".equals(p_tagName))
        {
            handleParentMarkerTag(p_tagLocation);
        }
        else if ("escape".equals(p_tagName))
        {
            handleEscapeTag(p_tagLocation);
        }
        else if ("generic".equals(p_tagName))
        {
            handleGenericTag(p_tagLocation);
        }
        else if ("annotate".equals(p_tagName))
        {
            handleAnnotationTag(p_tagLocation);
        }
        else
        {
            if (checkForTagClosure(p_tagLocation))
            {
                addError(p_tagLocation, "Unknown tag <%" + p_tagName + ">");
            }
        }
    }

    /**
     * Handle a tag closure
     * @param p_tagName The tag name
     * @param p_tagLocation The tag location
     * @throws IOException
     */
    @SuppressWarnings("unused") protected void handleTagClose(
        final String p_tagName,
        final org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        addError(p_tagLocation, "Unexpected tag close </%" + p_tagName + ">");
    }

    /**
     * This method is called when an end of file is reached, and should add an
     * error if this is not acceptable
     **/
    abstract protected void handleEof();

    /**
     * Handle the occurence of a '&lt;/|;&gt;' tag
     * @return <code>true</code> if this parser is done
     * @throws IOException
     **/
    @SuppressWarnings("unused")
    protected boolean handleNamedFragmentClose(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        addError(p_tagLocation, UNEXPECTED_NAMED_FRAGMENT_CLOSE_ERROR);
        return false;
    }

    /**
     * Handle the occurence of a '&lt;/&amp;&gt;' tag
     * @return <code>true</code> if this parser is done
     * @throws IOException
     **/
    @SuppressWarnings("unused")
    protected boolean handleFragmentsClose(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        addError(p_tagLocation, UNEXPECTED_FRAGMENTS_CLOSE_ERROR);
        return false;
    }

    /**
     * @param p_tagLocation location of the def tag
     */
    @SuppressWarnings("unused")
    protected void handleMethodTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(
            p_tagLocation,
            "<%method> sections only allowed at the top level of a document");
    }

    /**
     * @param p_tagLocation location of the def tag
     */
    @SuppressWarnings("unused")
    protected void handleOverrideTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(
            p_tagLocation,
            "<%override> sections only allowed at the top level of a document");
    }

    /**
     * @param p_tagLocation location of the def tag
     */
    @SuppressWarnings("unused")
    protected void handleDefTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(
            p_tagLocation,
            "<%def> sections only allowed at the top level of a document");
    }

    @SuppressWarnings("unused")
    protected void handleAbsMethodTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(
            p_tagLocation,
            "<%absmeth> sections only allowed at the top level of a document");
    }

    private class ConditionEndDetector implements TagEndDetector
    {
        public ConditionEndDetector(String p_tagName)
        {
           m_tagName = p_tagName;
        }

        public int checkEnd(char p_char)
        {
            switch (p_char)
            {
                case '%' :
                    m_seenPercent = true;
                    return 0;
                case '>' :
                    if (m_seenPercent)
                    {
                        return 2;
                    }
                    else
                    {
                       m_seenPercent = false;
                       return 0;
                    }
                default :
                   m_seenPercent = false;
                   return 0;
            }
        }

        public ParserErrorImpl getEofError(org.jamon.api.Location p_startLocation)
        {
            return new ParserErrorImpl(
               p_startLocation,
               "Reached end of file while reading " + m_tagName + " tag");
        }

        public void resetEndMatch()
        {
        }

        private boolean m_seenPercent = false;
        private final String m_tagName;
    }


    protected void handleWhileTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        try
        {
            m_root.addSubNode(new WhileParser(
                new WhileNode(
                    p_tagLocation, readCondition(p_tagLocation, "while")),
                m_reader,
                m_errors)
                .parse()
                .getRootNode());
        }
        catch (ParserErrorImpl e)
        {
            addError(e);
        }
    }

    protected void handleForTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        try
        {
            m_root.addSubNode(new ForParser(
                new ForNode(p_tagLocation, readCondition(p_tagLocation, "for")),
                m_reader,
                m_errors)
                .parse()
                .getRootNode());
        }
        catch (ParserErrorImpl e)
        {
            addError(e);
        }
    }

    protected void handleIfTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        try
        {
            IfParser parser = new IfParser(
                new IfNode(p_tagLocation,
                           readCondition(p_tagLocation, "if")),
                m_reader,
                m_errors);
            parser.parse();
            for ( ; parser != null; parser = parser.getContinuation() )
            {
                m_root.addSubNode(parser.getRootNode());
            }
        }
        catch (ParserErrorImpl e)
        {
            addError(e);
        }
    }

    protected String readCondition(org.jamon.api.Location p_tagLocation, String p_tagName)
        throws IOException, ParserErrorImpl
    {
        if (!soakWhitespace())
        {
            throw new ParserErrorImpl(
                p_tagLocation, "Malformed <%" + p_tagName + " ...%> tag");
        }
        else
        {
            return readJava(
                p_tagLocation,
                new ConditionEndDetector("<%" + p_tagName + " ...%>"));
        }
    }

    @SuppressWarnings("unused")
    protected void handleElseTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, ENCOUNTERED_ELSE_TAG_WITHOUT_PRIOR_IF_TAG);
    }

    @SuppressWarnings("unused")
    protected void handleElseIfTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, ENCOUNTERED_ELSEIF_TAG_WITHOUT_PRIOR_IF_TAG);
    }

    @SuppressWarnings("unused")
    protected void handleParentArgsNode(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        addError(p_tagLocation, PARENT_ARGS_TAG_IN_SUBCOMPONENT);
    }

    @SuppressWarnings("unused")
    protected void handleParentMarkerTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        addError(p_tagLocation, PARENT_MARKER_TAG_IN_SUBCOMPONENT);
    }

    @SuppressWarnings("unused")
    protected void handleEscapeTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, ESCAPE_TAG_IN_SUBCOMPONENT);
    }

    @SuppressWarnings("unused")
    protected void handleGenericTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, GENERIC_TAG_IN_SUBCOMPONENT);
    }

    @SuppressWarnings("unused")
    protected void handleAnnotationTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, ANNOTATE_TAG_IN_SUBCOMPONENT);
    }

    private void handleJavaTag(final org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (readChar('>'))
        {
            handleJavaCode(p_tagLocation, new JavaTagEndDetector());
        }
        else
        {
            soakWhitespace();
            handleJavaCode(p_tagLocation, new JavaSnippetTagEndDetector());
        }
    }

    private void handleJavaCode(final org.jamon.api.Location p_tagLocation, TagEndDetector p_endTagDetector)
    throws IOException
    {
        try
        {
            m_root.addSubNode(
                new JavaNode(
                    p_tagLocation,
                    readJava(p_tagLocation, p_endTagDetector)));
            soakWhitespace();
        }
        catch (ParserErrorImpl e)
        {
            addError(e);
        }
    }

    private static class JavaTagEndDetector extends AbstractTagEndDetector
    {
        public JavaTagEndDetector()
        {
            super("</%java>");
        }

    }

    protected static class JavaSnippetTagEndDetector extends AbstractTagEndDetector
    {
        protected JavaSnippetTagEndDetector()
        {
            super("%>");
        }

    }

    protected void handleLiteralTag(final org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            m_root.addSubNode(
                new LiteralNode(
                    p_tagLocation,
                    readUntil("</%LITERAL>", p_tagLocation)));
        }
    }

    @SuppressWarnings("unused")
    protected void handleClassTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, CLASS_TAG_IN_SUBCOMPONENT);
    }

    @SuppressWarnings("unused")
    protected void handleExtendsTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, EXTENDS_TAG_IN_SUBCOMPONENT);
    }

    @SuppressWarnings("unused")
    protected void handleImplementsTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        addError(p_tagLocation, IMPLEMENTS_TAG_IN_SUBCOMPONENT);
    }

    @SuppressWarnings("unused")
    protected void handleImportTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, IMPORT_TAG_IN_SUBCOMPONENT);
    }

    @SuppressWarnings("unused")
    protected void handleAliasesTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, ALIASES_TAG_IN_SUBCOMPONENT);
    }

    private void handleDocTag(org.jamon.api.Location p_tagLocation) throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            m_root.addSubNode(
                new DocNode(p_tagLocation, readUntil("</%doc>", p_tagLocation)));
        }
        soakWhitespace();
    }

    protected String readTagName() throws IOException
    {
        StringBuilder buffer = new StringBuilder();
        int c;
        while ((c = m_reader.read()) >= 0
            && !Character.isWhitespace((char) c)
            && c != '>')
        {
            buffer.append((char) c);
        }
        if (c >= 0)
        {
            m_reader.unread(c);
        }
        return buffer.toString();
    }

    protected String readLine() throws IOException
    {
        int c;
        StringBuilder line = new StringBuilder();
        while ((c = m_reader.read()) >= 0)
        {
            line.append((char) c);
            if (c == '\n')
            {
                break;
            }
        }
        return line.toString();
    }

    public Node getRootNode()
    {
        return m_root;
    }

    protected StringBuilder m_text = new StringBuilder();
    protected final Node m_root;
    protected final org.jamon.api.Location m_bodyStart;
    private boolean m_doneParsing;
}
