package org.jamon.api;


/**
 * A location in a Jamon template.
 */
public interface Location
{
    /**
     * @return the location of the template.
     */
    TemplateLocation getTemplateLocation();

    /**
     * @return the column of the location (1-based).
     */
    int getColumn();

    /**
     * @return the row of the location (1-based).
     */
    int getLine();
}