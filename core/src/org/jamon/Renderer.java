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

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

/**
 * A simple abstract class which lacks only a Writer to which to render.
 */
public abstract class Renderer
{
    /**
     * Render to the given writer.
     *
     * @param p_writer the Writer to which to render
     *
     * @exception IOException if writing to the Writer throws an IOException
     */
    public abstract void renderTo(Writer p_writer)
        throws IOException;


    /**
     * Render this Renderer into a String.
     *
     * @return a String that is the result of rendering this Renderer
     */
    public final String asString()
        throws IOException
    {
        StringWriter writer = new StringWriter();
        renderTo(writer);
        return writer.toString();
    }
}
