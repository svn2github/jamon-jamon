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
 * The Initial Developer of the Original Code is Luis O'Shea.  Portions
 * created by Luis O'Shea are Copyright (C) 2003 Luis O'Shea.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;

public abstract class AbstractCharacterEscaping
    implements Escaping
{

    public void write(String p_string, Writer p_writer)
        throws IOException
    {
        for (int i = 0; i < p_string.length(); i++)
        {
            write(p_string.charAt(i), p_writer);
        }
    }

    protected abstract void write(char p_char, Writer p_writer)
        throws IOException;

}
