package org.jamon.compiler;

import org.jamon.api.ParserError;

/**
 * @author ian
 **/
public class ParserErrorImpl extends Exception implements ParserError
{
    public ParserErrorImpl(org.jamon.api.Location p_location, String p_message)
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
    public org.jamon.api.Location getLocation()
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

    private final org.jamon.api.Location m_location;
    private final String m_message;

    @Override public boolean equals(Object p_obj)
    {
        return p_obj != null
            && p_obj instanceof ParserErrorImpl
            && m_location.equals(((ParserErrorImpl) p_obj).m_location)
            && m_message.equals(((ParserErrorImpl) p_obj).m_message);
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

    private static final long serialVersionUID = 2006091701L;
}
