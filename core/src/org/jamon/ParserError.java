package org.jamon;

import org.jamon.node.Location;

/**
 * @author ian
 **/
public class ParserError extends Exception
{
    public ParserError(Location p_location, String p_message)
    {
        if ((m_location = p_location) == null
            || (m_message = p_message) == null)
        {
            throw new NullPointerException();
        }
    }

    /**
     * @return The location of the error
     */
    public Location getLocation()
    {
        return m_location;
    }

    /**
     * @return The error message
     */
    @Override public String getMessage()
    {
        return m_message;
    }

    private final Location m_location;
    private final String m_message;

    @Override public boolean equals(Object p_obj)
    {
        return p_obj != null
            && p_obj instanceof ParserError
            && m_location.equals(((ParserError) p_obj).m_location)
            && m_message.equals(((ParserError) p_obj).m_message);
    }

    @Override public int hashCode()
    {
        return m_location.hashCode() ^ m_message.hashCode();
    }

    @Override public String toString()
    {
        return getLocation().getTemplateLocation() + ":"
               + getLocation().getLine() + ":"
               + getLocation().getColumn() + ": "
               + getMessage();
    }
}
