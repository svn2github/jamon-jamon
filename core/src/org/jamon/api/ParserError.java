package org.jamon.api;


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