/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
