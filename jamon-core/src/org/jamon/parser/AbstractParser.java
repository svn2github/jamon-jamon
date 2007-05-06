package org.jamon.parser;

import java.io.IOException;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractPathNode;

/**
 * @author ian
 **/
public class AbstractParser
{
    public static final String MALFORMED_TAG_ERROR = "Malformed tag";
    public static final String EOF_IN_JAVA_QUOTE_ERROR =
        "Reached end of file while inside a java quote";
    public static final String NOT_AN_IDENTIFIER_ERROR =
        "identifier exepected";
    public static final String BAD_JAVA_TYPE_SPECIFIER = "Bad java type specifier";
    public static final String BAD_ARGS_CLOSE_TAG = "malformed </%args> tag";
    public static final String INCOMPLETE_ARRAY_SPECIFIER_ERROR = "Expecting ']'";

    public AbstractParser(
        PositionalPushbackReader p_reader,
        ParserErrorsImpl p_errors)
    {
        m_reader = p_reader;
        m_errors = p_errors;
    }

    /**
     * Soak up all whitespace from the reader until non-whitespace or EOF is
     * encountered.
     * @return Whether or not any whitespace was encountered
     **/
    protected boolean soakWhitespace() throws IOException
    {
        int c;
        boolean whitespaceSeen = false;
        while ((c = m_reader.read()) >= 0 && Character.isWhitespace((char) c))
        {
            whitespaceSeen = true;
        }
        m_reader.unread(c);
        return whitespaceSeen;
    }

    protected void addError(org.jamon.api.Location p_location, String p_message)
    {
        m_errors.addError(new ParserErrorImpl(p_location, p_message));
    }

    protected void addError(ParserErrorImpl p_error)
    {
        m_errors.addError(p_error);
    }

    protected static class NotAnIdentifierException extends Exception {

        private static final long serialVersionUID = 2006091701L;

    }

    /**
     * Reads in a java identifier.
     * @return The identifier read.
     * @throws IOException
     * @throws NotAnIdentifierException if no identifier is found
     */
    protected String readIdentifierOrThrow()
        throws IOException, NotAnIdentifierException
    {
        int c;
        StringBuilder builder = new StringBuilder();
        if ((c = m_reader.read()) <= 0
            || !Character.isJavaIdentifierStart((char) c))
        {
            m_reader.unread(c);
            throw new NotAnIdentifierException();
        }
        else
        {
            builder.append((char) c);
        }

        while ((c = m_reader.read()) >= 0
            && Character.isJavaIdentifierPart((char) c))
        {
            builder.append((char) c);
        }
        m_reader.unread(c);
        return builder.toString();
    }

    /**
     * Read in a java identifier.
     * @param p_addErrorIfNoneFound if true, and no identifier is found, then call
     * {@link #addError(ParserErrorImpl)}
     * @return the identifier read, or the empty string if no identifier was found.
     * @throws IOException
     */
    protected String readIdentifier(boolean p_addErrorIfNoneFound) throws IOException
    {
        try
        {
            return readIdentifierOrThrow();
        }
        catch(NotAnIdentifierException e)
        {
            if (p_addErrorIfNoneFound)
            {
                addError(m_reader.getNextLocation(), NOT_AN_IDENTIFIER_ERROR);
            }
            return "";
        }
    }

    protected final PositionalPushbackReader m_reader;
    protected final ParserErrorsImpl m_errors;
    /**
         * Read a single character from the reader.  If it is the expected
         * character, return true; otherwise, unread it and return false
         * @param p_char The expected character.
         * @return True if the character read was that expected.
         * @throws IOException
         */
    protected boolean readChar(char p_char) throws IOException
    {
        int c;
        if ((c = m_reader.read()) == p_char)
        {
            return true;
        }
        else
        {
            m_reader.unread(c);
            return false;
        }
    }

    /**
     * Read from the reader until encountering an end marker
     * @param end The end marker for the string
     * @param startLocation The location marking the start of the block being
     *         read - only used for construcing error messages.
     * @param passOverQuotes True if the material being read is java which
     *         might contain the ending token inside quotes
     **/
    protected String readUntil(String p_end, org.jamon.api.Location p_startLocation)
        throws IOException
    {
        StringBuilder buffer = new StringBuilder();
        int charsSeen = 0;
        int c = -1;
        while (charsSeen < p_end.length() && (c = m_reader.read()) >= 0)
        {
            if (p_end.charAt(charsSeen) == c)
            {
                charsSeen++;
            }
            else if (charsSeen > 0)
            {
                buffer.append(p_end.substring(0, charsSeen));
                charsSeen = 0;
                m_reader.unread(c);
            }
            else
            {
                buffer.append((char) c);
            }
        }
        if (c < 0)
        {
            addError(p_startLocation, eofErrorMessage(p_end));
        }
        return buffer.toString();
    }

    public static String eofErrorMessage(String p_end)
    {
        return "Reached end of file while looking for '" + p_end + "'";
    }

    protected String readJava(
        org.jamon.api.Location p_startLocation,
        TagEndDetector p_tagEndDetector)
        throws IOException, ParserErrorImpl
    {
        StringBuilder buffer = new StringBuilder();
        int c = -1;
        boolean inString = false;
        boolean inChar = false;
        org.jamon.api.Location quoteStart = null;
        while ((c = m_reader.read()) >= 0)
        {
            switch (c)
            {
                case '"' :
                    inString = !inChar && !inString;
                    if (inString)
                    {
                        quoteStart = m_reader.getLocation();
                        p_tagEndDetector.resetEndMatch();
                    }
                    else
                    {
                        quoteStart = null;
                    }
                    break;
                case '\'' :
                    inChar = !inString && !inChar;
                    if (inChar)
                    {
                        quoteStart = m_reader.getLocation();
                        p_tagEndDetector.resetEndMatch();
                    }
                    else
                    {
                        quoteStart = null;
                    }
                    break;
                case '\\' :
                    if (inString || inChar)
                    {
                        buffer.append((char) c);
                        if ((c = m_reader.read()) < 0)
                        {
                            m_reader.unread(c);
                            // pick up the EOF the next time
                        }
                    }
                    break;
            }
            buffer.append((char) c);
            int endTokenLength;
            if (!(inString || inChar)
                && (endTokenLength = p_tagEndDetector.checkEnd((char) c)) > 0)
            {
                buffer.delete(
                    buffer.length() - endTokenLength,
                    buffer.length());
                return buffer.toString();
            }
        }
        if (quoteStart != null)
        {
            throw new ParserErrorImpl(quoteStart, EOF_IN_JAVA_QUOTE_ERROR);
        }
        else
        {
            throw p_tagEndDetector.getEofError(p_startLocation);
        }
    }

    protected boolean checkForTagClosure(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if (readChar('>'))
        {
            return true;
        }
        else
        {
            addError(p_tagLocation, MALFORMED_TAG_ERROR);
            return false;
        }
    }

    /**
     * @param p_token The token or token fragment we expect to see
     * @return True if we see that token or token fragment
     **/
    protected boolean checkToken(String p_token) throws IOException
    {
        for (int i = 0; i < p_token.length(); i++)
        {
            if (!readChar(p_token.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }

    protected String readType(final org.jamon.api.Location p_location) throws IOException
    {
        try
        {
            return new TypeNameParser(p_location, m_reader, m_errors).getType();
        }
        catch (ParserErrorImpl e)
        {
            addError(e);
            return "";
        }
    }

    protected String readClassName(final org.jamon.api.Location p_location) throws IOException
    {
        try
        {
            return new ClassNameParser(p_location, m_reader, m_errors).getType();
        }
        catch (ParserErrorImpl e)
        {
            addError(e);
            return "";
        }
    }

    protected AbstractPathNode parsePath() throws IOException
    {
        return new PathParser(m_reader, m_errors).getPathNode();
    }

    /**
     * Determine if the next character is a particular one, and if so, read and
     * append it to a StringBuilder.  Otherwise, do nothing.
     * @param p_char The character being looked for
     * @param builder The StringBuilder
     * @return true if the character matched and was appended.
     * @throws IOException
     */
    protected boolean readAndAppendChar(char p_char, StringBuilder builder) throws IOException
    {
        if (readChar(p_char))
        {
            builder.append(p_char);
            return true;
        }
        else
        {
            return false;
        }
    }
}