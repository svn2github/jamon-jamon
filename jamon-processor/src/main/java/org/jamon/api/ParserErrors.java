package org.jamon.api;

import java.util.List;


/**
 * A collection of errors encountered while parsing a Jamon template.
 */
public interface ParserErrors
{
    /**
     * Wheter any errors were encountered during template parsing.
     * @return true if any errors were encountered.
     */
    boolean hasErrors();

    /**
     * Get a list of all errors encountered during parsing.
     * @return a list of all errors encountered during parsing.
     */
    List<ParserError> getErrors();

    /**
     * Get a String representation of the errors encountered.  This is usefull in contexts where
     * only a single error can be reported, such as ant builds.
     * @return a String representation of errors encountered during parsing.
     */
    String getMessage();
}