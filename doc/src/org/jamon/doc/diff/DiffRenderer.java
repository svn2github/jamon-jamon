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

import java.util.List;
import java.util.LinkedList;

import bmsi.util.Diff;

public class DiffRenderer
{

    public DiffRenderer(Object[] p_a, Object[] p_b)
    {
        m_a = p_a;
        m_b = p_b;
        Diff diff = new Diff(m_a, m_b);
        diff.no_discards = true;  // try to make edit script as short as possible
        m_change = diff.diff_2(false);
    }

    public String render(Printer p_idemPrinter, 
                         Printer p_insertionPrinter, 
                         Printer p_deletionPrinter)
    {
        StringBuffer result = new StringBuffer();
        Diff.change current = m_change;
        int last = 0;
        while (current != null)
        {
            List idem = new LinkedList();
            for (int i = last; i < current.line1; i++)
            {
                idem.add(m_b[i]);
            }
            result.append(p_idemPrinter.print(idem));
            List insertion = new LinkedList();
            for (int i = 0; i < current.inserted; i++)
            {
                insertion.add(m_b[current.line1 + i]);
            }
            result.append(p_insertionPrinter.print(insertion));
            List deletion = new LinkedList();
            for (int i = 0; i < current.deleted; i++)
            {
                deletion.add(m_a[current.line0 + i]);
            }
            result.append(p_deletionPrinter.print(deletion));
            last = current.line1 + current.inserted;
	    current = current.link;
        }
        // print out what is left over
        List idem = new LinkedList();
        for (int i = last; i < m_b.length; i++)
        {
            idem.add(m_b[i]);
        }
        result.append(p_idemPrinter.print(idem));
        return result.toString();
    }

    private Object[] m_a, m_b;
    private Diff.change m_change;

}
