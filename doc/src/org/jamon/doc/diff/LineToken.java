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

public class LineToken
{

    public LineToken(String p_line)
    {
        m_line = p_line;
        m_trimmedLine = m_line.trim();
    }

    public String getValue()
    {
        return m_line + '\n';
    }

    public String toString()
    {
        return '[' + m_trimmedLine + ']';
    }

    public int hashCode()
    {
        return m_trimmedLine.hashCode();
    }

    public boolean equals(Object p_object)
    {
        if (p_object instanceof LineToken)
        {
            return m_trimmedLine.equals(((LineToken)p_object).m_trimmedLine);
        }
        return false;
    }

    private final String m_line, m_trimmedLine;

}
