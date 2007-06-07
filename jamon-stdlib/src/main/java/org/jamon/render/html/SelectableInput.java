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

@Deprecated public class SelectableInput
    extends Input
{
    public SelectableInput(String p_name, String p_value)
    {
        this(p_name, p_value, false);
    }

    public SelectableInput(String p_name,
                           String p_value,
                           String p_selectedValue)
    {
        this(p_name, p_value, p_value.equals(p_selectedValue));
    }

    public SelectableInput(String p_name,
                           String p_value,
                           boolean p_selected)
    {
        super(p_name, p_value);
        m_selected = p_selected;
    }

    public boolean isSelected()
    {
        return m_selected;
    }

    private final boolean m_selected;
}
