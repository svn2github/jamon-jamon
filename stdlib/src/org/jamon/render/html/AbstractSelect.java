/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.render.html;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class AbstractSelect
    extends AbstractInput
    implements Select
{
    public static abstract class Item
        implements Select.Item
    {
        public abstract Object getRenderable();
        public abstract String getValue();
        public abstract boolean isSelected();
        public final String getName()
        {
            return m_select.getName();
        }
        protected AbstractSelect getSelect()
        {
            return m_select;
        }
        private void setSelect(AbstractSelect p_select)
        {
            m_select = p_select;
        }
        private AbstractSelect m_select;
    }

    public Select.Item[] getItems()
    {
        return m_items;
    }

    public interface ItemMaker
    {
        Select.Item makeItem(Object data);
    }

    protected AbstractSelect(String p_name,
                             Object[] p_data,
                             ItemMaker p_maker)
    {
        this(p_name, Arrays.asList(p_data).iterator(), p_maker);
    }

    protected AbstractSelect(String p_name,
                             Iterator p_data,
                             ItemMaker p_maker)
    {
        this(p_name, create(p_data, p_maker));
    }

    protected AbstractSelect(String p_name,
                             Item[] p_items)
    {
        super(p_name);
        m_items = p_items;
        for(int i = 0; i < m_items.length; ++i)
        {
            m_items[i].setSelect(this);
        }
    }

    private static Item[] create( Iterator p_data, ItemMaker p_maker)
    {
        ArrayList items = new ArrayList();
        while( p_data.hasNext() )
        {
            items.add( p_maker.makeItem( p_data.next() ) );
        }
        return (Item[]) items.toArray(new Item[0]);
    }

    private final Item[] m_items;
}
