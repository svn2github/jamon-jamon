package org.jamon.html;

import java.util.Iterator;

public abstract class AbstractSelect
    extends AbstractInput
    implements Select
{
    protected AbstractSelect(String p_name)
    {
        super(p_name);
    }

    public abstract Iterator getValues();

    public abstract Object getRenderable(Object p_value);

    public abstract boolean isSelected(Object p_value);
}
