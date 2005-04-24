package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.node.AbstractBodyNode;
import org.jamon.node.DefaultEscapeNode;
import org.jamon.node.EmitNode;
import org.jamon.node.EscapeNode;
import org.jamon.node.JavaNode;
import org.jamon.node.Location;
import org.jamon.node.TextNode;

/**
 * @author ian
 **/

public abstract class AbstractBodyParser extends AbstractParser
{
    public static final String ESCAPE_TAG_IN_SUBCOMPONENT =
        "<%escape> tags only allowed at the top level of a document";
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
    public static final String EMIT_EOF_ERROR =
        "Reached end of file while reading emit value";
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
        AbstractBodyNode p_rootNode,
        PositionalPushbackReader p_reader,
        ParserErrors p_errors)
        throws IOException
    {
        super(p_reader, p_errors);
        m_root = p_rootNode;
        m_bodyStart = m_reader.getNextLocation();
        parse();
    }

    protected void handleText()
    {
        if (m_text.length() > 0)
        {
            m_root.addSubNode(
                new TextNode(
                    m_reader.getCurrentNodeLocation(),
                    m_text.toString()));
            m_text = new StringBuffer();
        }
        m_reader.markNodeBeginning();
    }

    protected void parse() throws IOException
    {
        int c;
        boolean doneParsing = false;
        m_reader.markNodeEnd();
        boolean isTopLevel = isTopLevel();
        while ((isTopLevel || !doneParsing) && (c = m_reader.read()) >= 0)
        {
            if (c == '<')
            {
                Location tagLocation = m_reader.getLocation();
                int c1 = m_reader.read();
                switch (c1)
                {
                    case '%' : // a tag or an emit
                        handleText();
                        if (soakWhitespace())
                        {
                            handleEmit(tagLocation);
                        }
                        else
                        {
                            handleTag(readTagName(), tagLocation);
                            m_reader.markNodeEnd();
                        }
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
                                doneParsing = true;
                                if (checkForTagClosure(tagLocation))
                                {
                                    handleTagClose(tagName, tagLocation);
                                }
                                break;
                            case '&' :
                                if (readChar('>'))
                                {
                                    doneParsing =
                                        handleFragmentsClose(tagLocation);
                                }
                                else
                                {
                                    m_text.append("</&");
                                }
                                break;
                            case '|' :
                                if (readChar('>'))
                                {
                                    doneParsing =
                                        handleNamedFragmentClose(tagLocation);
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
        if (!(doneParsing || isTopLevel))
        {
            handleEof();
        }
    }

    private class EmitEndDetector implements TagEndDetector
    {
        public int checkEnd(char p_char)
        {
            switch (p_char)
            {
                case '%' :
                    return 0;
                case '>' :
                    defaultEscaping = true;
                    return 2;
                case '#' :
                    defaultEscaping = false;
                    return 1;
                default :
                    return 0;
            }
        }

        public ParserError getEofError(Location p_startLocation)
        {
            return new ParserError(p_startLocation, EMIT_EOF_ERROR);
        }

        public void resetEndMatch()
        {
        }

        private boolean defaultEscaping = false;
    }

    /**
     * @param tagLocation Start of the emit.
     **/
    private void handleEmit(Location p_tagLocation) throws IOException
    {
        try
        {
            EmitEndDetector endDetector = new EmitEndDetector();
            String emitExpr = readJava(p_tagLocation, endDetector);
            if (endDetector.defaultEscaping)
            {
                m_root.addSubNode(
                    new EmitNode(
                        p_tagLocation,
                        emitExpr,
                        new DefaultEscapeNode(m_reader.getLocation())));
            }
            else
            {
                Location escapingLocation = m_reader.getLocation();
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
        catch (ParserError e)
        {
            addError(e);
        }
        m_reader.markNodeEnd();
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
        final Location p_tagLocation)
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
        else if ("args".equals(p_tagName))
        {
            //FIXME - either all handlers should throw these, or none.
            try
            {
                m_root.addSubNode(
                    new ArgsParser(m_reader, m_errors, p_tagLocation)
                        .getArgsNode());
            }
            catch (ParserError e)
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
            catch (ParserError e)
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
    protected void handleTagClose(
        final String p_tagName,
        final Location p_tagLocation)
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
    protected boolean handleNamedFragmentClose(Location p_tagLocation)
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
    protected boolean handleFragmentsClose(Location p_tagLocation)
        throws IOException
    {
        addError(p_tagLocation, UNEXPECTED_FRAGMENTS_CLOSE_ERROR);
        return false;
    }

    /**
     * @param p_tagLocation location of the def tag
     */
    protected void handleMethodTag(Location p_tagLocation) throws IOException
    {
        addError(
            p_tagLocation,
            "<%method> sections only allowed at the top level of a document");
    }

    /**
     * @param p_tagLocation location of the def tag
     */
    protected void handleOverrideTag(Location p_tagLocation) throws IOException
    {
        addError(
            p_tagLocation,
            "<%override> sections only allowed at the top level of a document");
    }

    /**
     * @param p_tagLocation location of the def tag
     */
    protected void handleDefTag(Location p_tagLocation) throws IOException
    {
        addError(
            p_tagLocation,
            "<%def> sections only allowed at the top level of a document");
    }

    protected void handleAbsMethodTag(Location p_tagLocation) 
        throws IOException
    {
        addError(
            p_tagLocation,
            "<%absmeth> sections only allowed at the top level of a document");
    }

    protected void handleParentArgsNode(Location p_tagLocation)
        throws IOException
    {
        addError(p_tagLocation, PARENT_ARGS_TAG_IN_SUBCOMPONENT);
    }
    
    protected void handleParentMarkerTag(Location p_tagLocation)
        throws IOException
    {
        addError(p_tagLocation, PARENT_MARKER_TAG_IN_SUBCOMPONENT);
    }
    
    protected void handleEscapeTag(Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, ESCAPE_TAG_IN_SUBCOMPONENT);
    }

    private static class JavaTagDetector implements TagEndDetector
    {
        private final static String END_TAG = "</%java>";
        private final static int END_TAG_LENGTH = END_TAG.length();
        int charsSeen = 0;

        public int checkEnd(final char p_char)
        {
            if (p_char == END_TAG.charAt(charsSeen))
            {
                if (++charsSeen == END_TAG_LENGTH)
                {
                    return charsSeen;
                }
            }
            else
            {
                charsSeen = 0;
            }
            return 0;
        }

        public ParserError getEofError(Location p_startLocation)
        {
            return new ParserError(
                p_startLocation,
                "Reached end of file while looking for '</%java>'");
        }

        public void resetEndMatch()
        {
            charsSeen = 0;
        }

    }

    protected void handleJavaTag(final Location p_tagLocation)
        throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            try
            {
                m_root.addSubNode(
                    new JavaNode(
                        p_tagLocation,
                        readJava(p_tagLocation, new JavaTagDetector())));
                soakWhitespace();
            }
            catch (ParserError e)
            {
                addError(e);
            }
        }
    }

    protected void handleLiteralTag(final Location p_tagLocation)
        throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            m_root.addSubNode(
                new TextNode(
                    p_tagLocation,
                    readUntil("</%LITERAL>", p_tagLocation)));
        }
    }

    protected void handleClassTag(Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, CLASS_TAG_IN_SUBCOMPONENT);
    }
    
    protected void handleExtendsTag(Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, EXTENDS_TAG_IN_SUBCOMPONENT);
    }

    protected void handleImplementsTag(Location p_tagLocation)
        throws IOException
    {
        addError(p_tagLocation, IMPLEMENTS_TAG_IN_SUBCOMPONENT);
    }

    protected void handleImportTag(Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, IMPORT_TAG_IN_SUBCOMPONENT);
    }

    protected void handleAliasesTag(Location p_tagLocation) throws IOException
    {
        addError(p_tagLocation, ALIASES_TAG_IN_SUBCOMPONENT);
    }

    private void handleDocTag(Location p_tagLocation) throws IOException
    {
        if (checkForTagClosure(p_tagLocation))
        {
            readUntil("</%doc>", p_tagLocation);
        }
        soakWhitespace();
    }

    protected static boolean isNewLine(final int p_c)
    {
        return p_c == '\n' || p_c == '\r';
    }

    protected String readTagName() throws IOException
    {
        StringBuffer buffer = new StringBuffer();
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
        StringBuffer line = new StringBuffer();
        while ((c = m_reader.read()) >= 0)
        {
            line.append((char) c);
            if (isNewLine(c))
            {
                break;
            }
        }
        return line.toString();
    }

    protected StringBuffer m_text = new StringBuffer();
    protected final AbstractBodyNode m_root;
    protected final Location m_bodyStart;
}
