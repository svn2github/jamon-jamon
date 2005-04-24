package org.jamon.parser;

import java.io.IOException;
import java.io.Reader;

import org.jamon.TemplateLocation;
import org.jamon.node.Location;

/**
 * A "pushback reader" which also tracks the current position in the file.  
 * Unlike {@link java.io.PushbackReader}, this class allows pushing back an EOF 
 * marker as well
 * @author ian
 **/

public class PositionalPushbackReader
{
    /**
     * @param p_templateLocation The path to the resource being read.
     * @param p_reader The underlying reader to use
     */
    public PositionalPushbackReader(
        TemplateLocation p_templateLocation, Reader p_reader)
    {
        m_reader = p_reader;
        m_templateLocation = p_templateLocation;
    }

    public int read() throws IOException
    {
        int c;
        if (m_pushedbackCharPending)
        {
            c = m_pushedbackChar;
            m_pushedbackCharPending = false;
        }
        else
        {
            c = m_reader.read();
        }
        m_previousLine = m_line;
        m_previousColumn = m_column;
        m_line = m_nextLine;
        m_column = m_nextColumn;
        switch (c)
        {
            case '\n' :
                if (!m_seenCr)
                {
                    m_ignoredNl = false;
                    newLine();
                }
                else
                {
                    m_seenCr = false;
                    m_ignoredNl = true;
                }
                break;
            case '\r' :
                m_ignoredNl = false;
                m_seenCr = true;
                newLine();
                break;
            default :
                m_ignoredNl = false;
                m_seenCr = false;
                m_nextColumn++;
        }
        return c;
    }

    private void newLine()
    {
        m_nextLine++;
        m_nextColumn = 1;
    }

    public void unread(int c) throws IOException
    {
        if (m_pushedbackCharPending)
        {
            throw new IOException("Trying to push back more than one character");
        }
        m_pushedbackChar = c;
        m_pushedbackCharPending = true;
        m_nextLine = m_line;
        m_nextColumn = m_column;
        m_line = m_previousLine;
        m_column = m_previousColumn;

        m_seenCr = m_ignoredNl;
    }

    /**
     * Get the location of the character just read.
     * @return The current location (line and column numbers starting at 1)
     */
    public Location getLocation()
    {
        return new Location(m_templateLocation, m_line, m_column);
    }

    /**
     * Get the location of the next character to be read (if there is one).
     * @return The location of the next character
     */
    public Location getNextLocation()
    {
        return new Location(m_templateLocation, m_nextLine, m_nextColumn);
    }

    /**
     * @return True if the character just read was at the begining of a line
     */
    public boolean isLineStart()
    {
        return m_column == 1;
    }

    /**
     * Mark that we are just starting a node.
     **/
    public void markNodeBeginning()
    {
        m_currentNodeLine = m_line;
        m_currentNodeColumn = m_column;
    }

    /**
     * Mark that we have just finished a node
     **/
    public void markNodeEnd()
    {
        m_currentNodeLine = m_nextLine;
        m_currentNodeColumn = m_nextColumn;
    }

    /**
     * Get the location of the current node, as set by 
     * {@link #markNodeBeginning()} or {@link #markNodeEnd()}
     * 
     * @return The location of the current node
     */
    public Location getCurrentNodeLocation()
    {
        return new Location(
            m_templateLocation, m_currentNodeLine, m_currentNodeColumn);
    }

    private final Reader m_reader;
    private final TemplateLocation m_templateLocation;
    boolean m_pushedbackCharPending = false;
    int m_pushedbackChar;
    private int m_line = 1, m_column = 1;
    private int m_nextLine = 1, m_nextColumn = 1;
    private int m_previousLine, m_previousColumn;
    private int m_currentNodeLine = 1, m_currentNodeColumn = 1;
    private boolean m_seenCr = false;
    private boolean m_ignoredNl = false;
}
