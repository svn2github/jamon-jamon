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

public class SingleSelect
    extends AbstractSelect
{
    public static abstract class Item
        extends AbstractSelect.Item
    {
        @Override public final boolean isSelected()
        {
            return getValue().equals(((SingleSelect) getSelect()).m_selectedValue);
        }
    }

    public SingleSelect(String p_name,
                        String p_selectedValue,
                        Object[] p_data,
                        ItemMaker p_maker)
    {
        super(p_name, p_data, p_maker);
        m_selectedValue = p_selectedValue;
    }

    public SingleSelect(String p_name,
                        String p_selectedValue,
                        Iterator p_data,
                        ItemMaker p_maker)
    {
        super(p_name, p_data, p_maker);
        m_selectedValue = p_selectedValue;
    }

    public SingleSelect(String p_name,
                        String p_selectedValue,
                        Item[] p_items)
    {
        super(p_name, p_items);
        m_selectedValue = p_selectedValue;
    }

    private final String m_selectedValue;
}
