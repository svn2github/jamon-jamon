/**
 *
 */
package org.jamon.parser;

import org.jamon.compiler.ParserErrorImpl;

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
    @Override
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

    @Override
    public ParserErrorImpl getEofError(org.jamon.api.Location p_startLocation)
    {
        return new ParserErrorImpl(
            p_startLocation,
            "Reached end of file while looking for '" + m_endTag + "'");
    }

    @Override
    public void resetEndMatch()
    {
        charsSeen = 0;
    }

}