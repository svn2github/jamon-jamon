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

package org.jamon;

import java.util.List;
import java.util.Iterator;

public class FragmentCallInfo
{
    public FragmentCallInfo(String p_name, List p_statements)
    {
        m_name = p_name;
        m_statements = p_statements;
    }

    public String getName()
    {
        return m_name;
    }

    public Iterator getStatements()
    {
        return m_statements.iterator();
    }

    private final String m_name;
    private final List m_statements;
}
