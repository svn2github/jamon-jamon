package org.jamon.api;

import java.util.List;


public interface ParserErrors
{
    boolean hasErrors();
    List<ParserError> getErrors();
    String getMessage();
}