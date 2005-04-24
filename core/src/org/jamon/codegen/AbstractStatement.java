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
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 */

package org.jamon.codegen;

import org.jamon.node.Location;

public abstract class AbstractStatement
    implements Statement
{
    protected AbstractStatement(Location p_location, String p_templateIdentifier)
    {
        m_location = p_location;
        m_templateIdentifier = p_templateIdentifier;
    }

    private final Location m_location;
    private final String m_templateIdentifier;

    protected final Location getLocation()
    {
        return m_location;
    }

    protected final String getTemplateIdentifier()
    {
        return m_templateIdentifier;
    }

    protected final void generateSourceLine(CodeWriter p_writer)
    {
        p_writer.printLocation(m_location);
    }

}
