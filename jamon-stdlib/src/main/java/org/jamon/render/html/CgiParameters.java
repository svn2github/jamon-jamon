/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.render.html;

import java.util.ArrayList;
import java.util.List;

@Deprecated public class CgiParameters
{
    public CgiParameters add( String p_name, String p_value )
    {
        m_inputs.add( new Input( p_name, p_value ) );
        return this;
    }

    public Input[] getInputs()
    {
        return m_inputs.toArray( new Input[m_inputs.size()] );
    }

    private final List<Input> m_inputs = new ArrayList<Input>();
}
