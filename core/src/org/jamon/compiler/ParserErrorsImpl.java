package org.jamon.compiler;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.jamon.api.ParserError;
import org.jamon.api.ParserErrors;

/**
 * @author ian
 **/
public final class ParserErrorsImpl extends IOException implements ParserErrors
{
    public ParserErrorsImpl() {}

    public ParserErrorsImpl(ParserError p_error)
    {
        m_errors.add(p_error);
    }

    public boolean hasErrors()
    {
        return ! m_errors.isEmpty();
    }

    public List<ParserError> getErrors()
    {
        return m_errors;
    }

    public void addError(ParserError p_error)
    {
        m_errors.add(p_error);
    }

    public void addError(String p_message, org.jamon.api.Location p_location)
    {
        addError(new ParserErrorImpl(p_location, p_message));
    }

    public void addErrors(ParserErrorsImpl p_errors)
    {
        m_errors.addAll(p_errors.m_errors);
    }

    private final List<ParserError> m_errors = new ArrayList<ParserError>();


    @Override public String getMessage()
    {
        StringBuilder buffer =
            new StringBuilder("Errors occured during parsing:");
        for (ParserError error: getErrors())
        {
            buffer.append("\n   ");
            buffer.append(error.toString());
        }
        return buffer.toString();
    }

    public void printErrors(PrintStream p_stream)
    {
        for (ParserError error: getErrors())
        {
            p_stream.println(error);
        }
    }

    private static final long serialVersionUID = 2006091701L;
}
