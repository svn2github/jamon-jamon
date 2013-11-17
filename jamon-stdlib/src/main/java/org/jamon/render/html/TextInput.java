/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.render.html;

@Deprecated public class TextInput
    extends Input
{
    public TextInput(String p_name)
    {
        this(p_name,null);
    }

    public TextInput(String p_name, String p_value)
    {
        this(p_name, p_value, 0);
    }

    public TextInput(String p_name, String p_value, int p_maxLength)
    {
        this(p_name, p_value, p_maxLength, false);
    }

    public TextInput(String p_name,
                     String p_value,
                     int p_maxLength,
                     boolean p_isUpperCaseOnly)
    {
        super(p_name, p_value);
        m_maxLength = p_maxLength;
        m_isUpperCaseOnly = p_isUpperCaseOnly;
    }

    public int getMaxLength()
    {
        return m_maxLength;
    }

    public boolean isUpperCaseOnly()
    {
        return m_isUpperCaseOnly;
    }

    private final int m_maxLength;
    private final boolean m_isUpperCaseOnly;
}
