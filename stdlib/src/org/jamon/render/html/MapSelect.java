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

package org.jamon.render.html;

import java.util.Iterator;
import java.util.Map;

public class MapSelect
    extends AbstractSelect
{
    public MapSelect(String p_name, Map p_options, Object p_default)
    {
        super(p_name);
        m_options = p_options;
        m_default = p_default;
    }

    public MapSelect(String p_name, Map p_options)
    {
        this(p_name, p_options, null);
    }

    public Iterator getValues()
    {
        return m_options.keySet().iterator();
    }

    public Object getRenderable(Object p_value)
    {
        return m_options.get(p_value);
    }

    public boolean isSelected(Object p_value)
    {
        return m_default != null && m_default.equals(p_value);
    }

    private final Map m_options;
    private final Object m_default;
}
