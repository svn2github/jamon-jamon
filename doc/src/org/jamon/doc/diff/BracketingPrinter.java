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

import java.util.Iterator;
import java.util.List;

public class BracketingPrinter
    implements Printer
{

    public BracketingPrinter()
    {
        this("", "");
    }

    public BracketingPrinter(String p_start, String p_end)
    {
        m_start = p_start;
        m_end = p_end;
    }
    
    public String print(List p_list)
    {
        if (p_list.isEmpty())
        {
            return "";
        }
        else
        {
            StringBuffer result = new StringBuffer();
            result.append(m_start);
            for (Iterator i = p_list.iterator(); i.hasNext(); )
            {
                result.append(((LineToken)i.next()).getValue());
            }
            result.append(m_end);
            return result.toString();
        }
    }
    
    private final String m_start, m_end;
    
}
