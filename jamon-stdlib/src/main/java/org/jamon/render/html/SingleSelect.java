/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.render.html;

import java.util.Iterator;

@Deprecated public class SingleSelect<Renderable>
    extends AbstractSelect<Renderable>
{
    @SuppressWarnings({"hiding", "deprecation"})
    @Deprecated
    public abstract static class Item<Renderable>
        extends AbstractSelect.Item<Renderable>
    {
        public final boolean isSelected()
        {
            return getValue()
               .equals(((SingleSelect) getSelect()).m_selectedValue);
        }
    }

    public <DataType> SingleSelect(
       String p_name,
       String p_selectedValue,
       DataType[] p_data,
       ItemMaker<? super DataType, Renderable> p_maker)
    {
        super(p_name, p_data, p_maker);
        m_selectedValue = p_selectedValue;
    }

    public <DataType> SingleSelect(
        String p_name,
        String p_selectedValue,
        Iterator<? extends DataType> p_data,
        ItemMaker<? super DataType, Renderable> p_maker)
    {
        super(p_name, p_data, p_maker);
        m_selectedValue = p_selectedValue;
    }

    public SingleSelect(String p_name,
                        String p_selectedValue,
                        Item<? extends Renderable>[] p_items)
    {
        super(p_name, p_items);
        m_selectedValue = p_selectedValue;
    }

    private final String m_selectedValue;
}
