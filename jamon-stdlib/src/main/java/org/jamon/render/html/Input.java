/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.render.html;

@Deprecated public class Input
    extends AbstractInput
{
    public Input(String p_name)
    {
        this(p_name,null);
    }

    public Input(String p_name, String p_value)
    {
        super(p_name);
        m_value = p_value;
    }

    public String getValue()
    {
        return m_value;
    }

    private final String m_value;
}
