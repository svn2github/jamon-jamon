package org.jamon.api;


public interface Location
{
    TemplateLocation getTemplateLocation();
    int getColumn();
    int getLine();
}