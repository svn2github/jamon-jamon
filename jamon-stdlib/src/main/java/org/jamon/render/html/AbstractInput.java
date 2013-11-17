/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.render.html;

@Deprecated public abstract class AbstractInput
{
    protected AbstractInput(String p_name)
    {
        m_name = p_name;
    }

    public String getName()
    {
        return m_name;
    }

    private final String m_name;
}
