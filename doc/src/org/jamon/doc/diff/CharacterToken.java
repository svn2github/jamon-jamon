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
 * The Initial Developer of the Original Code is Luis O'Shea.  Portions
 * created by Luis O'Shea are Copyright (C) 2002 Luis O'Shea.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.doc.diff;

public class CharacterToken
    implements Token
{
    public CharacterToken(char p_char)
    {
        m_char = p_char;
    }
    
    public String getValue()
    {
        return String.valueOf(m_char);
    }
    
    public String toString()
    {
        return "'" + m_char + "'";
    }
    
    public int hashCode()
    {
        return (int)m_char;
    }
    
    public boolean equals(Object p_object)
    {
        if (p_object instanceof CharacterToken)
        {
            return m_char == ((CharacterToken)p_object).m_char;
        }
        else
        {
            return false;
        }
    }
    
    private final char m_char;
}
