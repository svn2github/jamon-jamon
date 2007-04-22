package org.jamon.node;

import org.jamon.api.Location;
import org.jamon.api.TemplateLocation;

/**
 * @author ian
 **/
public final class LocationImpl implements Location
{
    public LocationImpl(
        TemplateLocation p_templateLocation, int p_line, int p_column)
    {
        m_templateLocation = p_templateLocation;
        m_line = p_line;
        m_column = p_column;
    }

    public TemplateLocation getTemplateLocation()
    {
        return m_templateLocation;
    }

    public int getColumn()
    {
        return m_column;
    }

    public int getLine()
    {
        return m_line;
    }

    private final TemplateLocation m_templateLocation;
    private final int m_line;
    private final int m_column;

    @Override public boolean equals(Object p_obj)
    {
        if (p_obj instanceof LocationImpl)
        {
            Location loc = (Location) p_obj;
            return loc.getTemplateLocation().equals(m_templateLocation)
                && loc.getLine() == m_line
                && loc.getColumn() == m_column;
        }
        else
        {
            return false;
        }
    }

    @Override public int hashCode()
    {
        return (m_line * 160 + m_column) ^ m_templateLocation.hashCode();
    }

    @Override public String toString()
    {
        return "Location{" + m_templateLocation + ": ("+ m_line + ":" + m_column + ")}";
    }
}
