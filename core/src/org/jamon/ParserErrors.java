package org.jamon;

import java.util.List;

import org.jamon.api.ParserError;

public interface ParserErrors
{
    boolean hasErrors();
    List<ParserError> getErrors();
    String getMessage();
}