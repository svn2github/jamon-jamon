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
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.eclipse;

/**
 * A location in a file.  The begining of the file has line and column number 1
 */
public class Location implements Comparable<Location>
{
    public Location(int p_line, int p_column)
    {
        m_line = p_line;
        m_column = p_column;
    }

    public int getLine() { return m_line; }
    public int getColumn() { return m_column; }

    public int compareTo(Location p_other)
    {
        int lineDelta = getLine() - p_other.getLine();
        return lineDelta == 0 ? getColumn() - p_other.getColumn() : lineDelta;
    }

    private final int m_line;
    private final int m_column;
}
