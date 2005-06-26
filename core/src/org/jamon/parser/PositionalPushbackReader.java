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
    private static class Position
    {
        void assign(Position p_position)
        {
            m_row = p_position.m_row;
            m_column = p_position.m_column;
            m_ignoredNewLine = p_position.m_ignoredNewLine;
            m_seenCarriageReturn = p_position.m_seenCarriageReturn;
        }
        
        public void handleNewLine()
        {
            if (!m_seenCarriageReturn)
            {
                m_ignoredNewLine = false;
                nextRow();
            }
            else
            {
                m_seenCarriageReturn = false;
                m_ignoredNewLine = true;
            }
        }
        
        public void handleCarriageReturn()
        {
            m_ignoredNewLine = false;
            m_seenCarriageReturn= true;
            nextRow();
        }
        
        public void nextColumn()
        {
            m_column++;
            m_ignoredNewLine = m_seenCarriageReturn = false;
        }
        
        public void nextRow()
        {
            m_row++;
            m_column = 1;
        }
        
        public Location location(TemplateLocation p_templateLocation)
        {
            return new Location(p_templateLocation, m_row, m_column);
        }
        
        public boolean isLineStart()
        {
            return m_column == 1;
        }
        
        private int m_row = 1;
        private int m_column = 1;
        private boolean m_ignoredNewLine = false, m_seenCarriageReturn = false;
    }
    
    /**
     * @param p_templateLocation The path to the resource being read.
     * @param p_reader The underlying reader to use
     */
    public PositionalPushbackReader(
        TemplateLocation p_templateLocation, Reader p_reader)
    {
        this(p_templateLocation, p_reader, 1);
    }

    public PositionalPushbackReader(TemplateLocation p_templateLocation,
                                    Reader p_reader,
                                    int p_pushbackBufferSize)
    {
        m_reader = p_reader;
        m_templateLocation = p_templateLocation;
        m_positions = new Position[p_pushbackBufferSize + 2];
        {
            for (int i = 0; i < m_positions.length; i++)
            {
                m_positions[i] = new Position();
            }
        }
        m_pushedbackChars = new int[p_pushbackBufferSize];
    }
    
    public int read() throws IOException
    {
        int c;
        if (m_pushedbackCharsPending > 0)
        {
            c = m_pushedbackChars[--m_pushedbackCharsPending];
        }
        else
        {
            c = m_reader.read();
        }
        for (int i = m_positions.length - 1; i > 0; i--)
        {
            m_positions[i].assign(m_positions[i-1]);
        }
        
        switch (c)
        {
            case '\n':
                m_positions[0].handleNewLine();
                break;
            case '\r':
                m_positions[0].handleCarriageReturn();
                break;
            default :
                m_positions[0].nextColumn(); 
        }
        return c;
    }

    public void unread(int c) throws IOException
    {
        if (m_pushedbackCharsPending >= m_pushedbackChars.length)
        {
            throw new IOException("Trying to push back characters than allowed");
        }
        m_pushedbackChars[m_pushedbackCharsPending++] = c;

        for (int i = 0; i < m_positions.length - 1; i++)
        {
            m_positions[i].assign(m_positions[i+1]);
        }
    }

    /**
     * Get the location of the character just read.
     * @return The current location (line and column numbers starting at 1)
     */
    public Location getLocation()
    {
        return m_positions[1].location(m_templateLocation);
    }

    /**
     * Get the location of the next character to be read (if there is one).
     * @return The location of the next character
     */
    public Location getNextLocation()
    {
        return m_positions[0].location(m_templateLocation);
    }

    /**
     * @return True if the character just read was at the begining of a line
     */
    public boolean isLineStart()
    {
        return m_positions[1].isLineStart();
    }

    /**
     * Mark that we are just starting a node.
     **/
    public void markNodeBeginning()
    {
        m_currentNodePosition.assign(m_positions[1]);
    }

    /**
     * Mark that we have just finished a node
     **/
    public void markNodeEnd()
    {
        m_currentNodePosition.assign(m_positions[0]);
    }

    /**
     * Get the location of the current node, as set by 
     * {@link #markNodeBeginning()} or {@link #markNodeEnd()}
     * 
     * @return The location of the current node
     */
    public Location getCurrentNodeLocation()
    {
        return m_currentNodePosition.location(m_templateLocation);
    }

    private final Reader m_reader;
    private final TemplateLocation m_templateLocation;
    int m_pushedbackCharsPending = 0;
    final int m_pushedbackChars[];
    
    private final Position[] m_positions;
    private Position m_currentNodePosition = new Position();
}
