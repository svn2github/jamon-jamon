package org.jamon.html;

import java.util.Iterator;

public interface Select
{
    String getName();

    Iterator getValues();

    Object getRenderable(Object p_value);

    boolean isSelected(Object p_value);
}
