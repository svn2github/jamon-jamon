/**
 *
 */
package org.jamon.parser;

import org.jamon.ParserError;
import org.jamon.node.Location;

class AbstractTagEndDetector implements TagEndDetector
{
    private final String m_endTag;
    private final int m_endTagLength;
    int charsSeen = 0;

    protected AbstractTagEndDetector(String p_endTag)
    {
        m_endTag = p_endTag;
        m_endTagLength = p_endTag.length();
    }
    public int checkEnd(final char p_char)
    {
        if (p_char == m_endTag.charAt(charsSeen))
        {
            if (++charsSeen == m_endTagLength)
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
            "Reached end of file while looking for '" + m_endTag + "'");
    }

    public void resetEndMatch()
    {
        charsSeen = 0;
    }

}