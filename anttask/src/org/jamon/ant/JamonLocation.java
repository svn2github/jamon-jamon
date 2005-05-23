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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.ant;

import org.apache.tools.ant.Location;

public class JamonLocation extends Location
{
    public JamonLocation(org.jamon.node.Location p_location)
    {
        super(p_location.getTemplateLocation().toString(),
              p_location.getLine(),
              p_location.getColumn());
        m_columnNumber = p_location.getColumn();
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer(super.toString());
        buf.insert(buf.length() - 2, ":");
        buf.insert(buf.length() - 2, m_columnNumber);
        return buf.toString();
    }

    private final int m_columnNumber;
}
