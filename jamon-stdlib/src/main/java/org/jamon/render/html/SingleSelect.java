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
 * Contributor(s): Ian Robertson
 */

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
