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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.render.html;

import java.util.Iterator;
import java.util.ArrayList;

public abstract class AbstractSelect
    extends AbstractInput
    implements Select
{
    public interface ItemMaker
    {
        Item makeItem(Object data);
    }

    protected AbstractSelect(String p_name,
                             Object[] p_data,
                             ItemMaker p_maker)
    {
        this(p_name, new Item[p_data.length]);
        for(int i = 0; i < m_items.length; ++i)
        {
            m_items[i] = p_maker.makeItem(p_data[i]);
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
    }

    public abstract boolean isSelected(Item item);

    public Item[] getItems()
    {
        return m_items;
    }

    private final Item[] m_items;
}
