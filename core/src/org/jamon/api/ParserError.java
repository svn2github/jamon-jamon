package org.jamon.api;


/**
 * An error encountered during the parsing of a Jamon template.
 */
public interface ParserError
{

    /**
     * @return The location of the error
     */
    public abstract org.jamon.api.Location getLocation();

    /**
     * @return The error message
     */
    public abstract String getMessage();

}