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
 * Contributor(s):
 */

package org.jamon.emit;

import java.util.Map;
import java.util.HashMap;

public class EmitMode
{
    private static Map s_modes = new HashMap();

    public static final EmitMode STANDARD =
        new EmitMode(StandardEmitter.class);
    public static final EmitMode LIMITED =
        new EmitMode(LimitedEmitter.class);
    public static final EmitMode STRICT =
        new EmitMode(StrictEmitter.class);

    public static EmitMode fromString(String p_string)
    {
        return (EmitMode) s_modes.get(p_string.toUpperCase());
    }

    public boolean equals(Object p_obj)
    {
        return (p_obj instanceof EmitMode)
            && ((EmitMode)p_obj).m_name.equals(m_name);
    }

    public int hashCode()
    {
        return m_name.hashCode();
    }

    public String getName()
    {
        return m_name;
    }

    public String getEmitterClassName()
    {
        return "org.jamon.emit." + m_name + "Emitter";
    }

    public String toString()
    {
        return "EmitMode{" + m_name + "}";
    }


    private static String extractModeName(Class p_class)
    {
        final String EMITTER = "Emitter";
        String name = p_class.getName();
        name = name.substring(name.lastIndexOf('.')+1);
        if (name.endsWith(EMITTER))
        {
            return name.substring(0, name.length() - EMITTER.length());
        }
        else
        {
            throw new IllegalArgumentException("Not an emitter class "
                                               + p_class);
        }
    }

    private EmitMode(Class p_emitterClass)
    {
        m_name = extractModeName(p_emitterClass);
        s_modes.put(m_name.toUpperCase(), this);
    }

    private final String m_name;
}
