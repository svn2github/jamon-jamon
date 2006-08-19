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
 * Contributor(s): Ian Robertson
 */

package org.jamon.emit;

public enum EmitMode
{
    STANDARD(StandardEmitter.class),
    LIMITED(LimitedEmitter.class),
    STRICT(StrictEmitter.class);

    public static EmitMode fromString(String p_string)
    {
        return valueOf(p_string.toUpperCase());
    }

    public String getEmitterClassName()
    {
        return m_class.getName();
    }

    @Override public String toString()
    {
        return "EmitMode{" + m_class.getName() + "}";
    }


    private EmitMode(Class<?> p_emitterClass)
    {
        m_class = p_emitterClass;
    }

    private final Class<?> m_class;
}
