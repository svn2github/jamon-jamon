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

public class TextInput
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
