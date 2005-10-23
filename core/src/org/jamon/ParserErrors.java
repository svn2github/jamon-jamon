package org.jamon;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jamon.node.Location;

/**
 * @author ian
 **/
public final class ParserErrors extends IOException
{
    public ParserErrors() {}

    public ParserErrors(ParserError p_error)
    {
        m_errors.add(p_error);
    }

    public boolean hasErrors()
    {
        return ! m_errors.isEmpty();
    }

    public Iterator<ParserError> getErrors()
    {
        return m_errors.iterator();
    }

    public void addError(ParserError p_error)
    {
        m_errors.add(p_error);
    }

    public void addError(String p_message, Location p_location)
    {
        addError(new ParserError(p_location, p_message));
    }

    public void addErrors(ParserErrors p_errors)
    {
        m_errors.addAll(p_errors.m_errors);
    }

    private final List<ParserError> m_errors = new ArrayList<ParserError>();


    @Override public String getMessage()
    {
        StringBuilder buffer =
            new StringBuilder("Errors occured during parsing:");
        for (Iterator<ParserError> i = getErrors(); i.hasNext(); )
        {
            buffer.append("\n   ");
            buffer.append(i.next().toString());
        }
        return buffer.toString();
    }

    public void printErrors(PrintStream p_stream)
    {
        for (Iterator<ParserError> i = getErrors(); i.hasNext(); )
        {
            ParserError error = i.next();
            System.err.println(error);

        }
    }

}
