package org.jamon.parser;

import org.jamon.compiler.ParserErrorImpl;

/**
 * A {@code TagEndDetector} which will end either on a "%>" sequence or a hash sign ("#").
 */
public class HashEndDetector implements TagEndDetector
{
    @Override
    public int checkEnd(char p_char)
    {
        switch (p_char)
        {
            case '%' :
                seenPercent = true;
                return 0;
            case '>' :
                if (seenPercent)
                {
                    endedWithHash = false;
                    return 2;
                }
                else
                {
                   seenPercent = false;
                   return 0;
                }
            case '#' :
                endedWithHash = true;
                return 1;
            default :
               seenPercent = false;
               return 0;
        }
    }

    @Override
    public ParserErrorImpl getEofError(org.jamon.api.Location p_startLocation)
    {
        return new ParserErrorImpl(p_startLocation, AbstractBodyParser.PERCENT_GREATER_THAN_EOF_ERROR);
    }

    @Override
    public void resetEndMatch() {}

    /**
     * @return {@code true} if the final character read was a hash ("#"), false if it was a "%>"
     * sequence.
     */
    public boolean endedWithHash()
    {
        return endedWithHash;
    }

    private boolean endedWithHash = false;
    private boolean seenPercent = false;
}