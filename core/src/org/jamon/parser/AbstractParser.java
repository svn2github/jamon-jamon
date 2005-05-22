package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.Location;

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
        ParserErrors p_errors)
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

    protected void addError(Location p_location, String p_message)
    {
        m_errors.addError(new ParserError(p_location, p_message));
    }

    protected void addError(ParserError p_error)
    {
        m_errors.addError(p_error);
    }

    protected class NotAnIdentifierException extends Exception {}
    
    /**
     * Reads in a java identifier.
     * @param p_throwExceptionOnFailure If true, and no identifier is found,
     *        then throw a NotAnIdentifierException. Otherwise, call addError.
     * @return The identifier read.
     * @throws IOException
     */
    protected String readIdentifierOrThrow() 
        throws IOException, NotAnIdentifierException
    {
        int c;
        StringBuffer buffer = new StringBuffer();
        if ((c = m_reader.read()) <= 0
            || !Character.isJavaIdentifierStart((char) c))
        {
            m_reader.unread(c);
            throw new NotAnIdentifierException();
        }
        else
        {
            buffer.append((char) c);
        }

        while ((c = m_reader.read()) >= 0
            && Character.isJavaIdentifierPart((char) c))
        {
            buffer.append((char) c);
        }
        m_reader.unread(c);
        return buffer.toString();
    }
    
    protected String readIdentifier() throws IOException
    {
        try
        {
            return readIdentifierOrThrow();
        }
        catch(NotAnIdentifierException e)
        {
            addError(m_reader.getNextLocation(), NOT_AN_IDENTIFIER_ERROR);
            return "";
        }
    }

    protected final PositionalPushbackReader m_reader;
    protected final ParserErrors m_errors;
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
    protected String readUntil(String p_end, Location p_startLocation)
        throws IOException
    {
        StringBuffer buffer = new StringBuffer();
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
            addError(
                p_startLocation,
                "Reached end of file while looking for '" + p_end + "'");
        }
        return buffer.toString();
    }

    protected String readJava(
        Location p_startLocation,
        TagEndDetector p_tagEndDetector)
        throws IOException, ParserError
    {
        StringBuffer buffer = new StringBuffer();
        int c = -1;
        boolean inString = false;
        boolean inChar = false;
        Location quoteStart = null;
        while ((c = m_reader.read()) >= 0)
        {
            switch (c)
            {
                case '"' :
                    if (inString = !inChar && !inString)
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
                    if (inChar = !inString && !inChar)
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
            throw new ParserError(quoteStart, EOF_IN_JAVA_QUOTE_ERROR);
        }
        else
        {
            throw p_tagEndDetector.getEofError(p_startLocation);
        }
    }

    protected boolean checkForTagClosure(Location p_tagLocation)
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

    protected String readType(final Location p_location) throws IOException
    {
        return new TypeNameParser(p_location, m_reader, m_errors).getType();
    }
    
    protected String readClassName(final Location p_location) throws IOException
    {
        return new ClassNameParser(p_location, m_reader, m_errors).getType();
    }
    
    protected String readImport(final Location p_location) throws IOException
    {
        return new ImportParser(p_location, m_reader, m_errors).getType();
    }

    protected AbstractPathNode parsePath() throws IOException
    {
        return new PathParser(m_reader, m_errors).getPathNode();
    }
}