/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.render.html;

import junit.framework.TestCase;

@Deprecated public class SelectableInputTest
    extends TestCase
{
    public void testConstruction3()
    {
        final String NAME = "foo";
        final String VALUE = "bar";
        verify(new SelectableInput(NAME, VALUE, VALUE),
               NAME,
               VALUE,
               true);
        verify(new SelectableInput(NAME, VALUE, VALUE+"x"),
               NAME,
               VALUE,
               false);
    }

    public void testConstruction2()
    {
        final String NAME = "foo";
        final String VALUE = "bar";
        verify(new SelectableInput(NAME, VALUE, true),
               NAME,
               VALUE,
               true);
        verify(new SelectableInput(NAME, VALUE, false),
               NAME,
               VALUE,
               false);
    }

    public void testConstruction1()
    {
        final String NAME = "foo";
        final String VALUE = "bar";
        verify(new SelectableInput(NAME, VALUE),
               NAME,
               VALUE,
               false);
    }

    private void verify(SelectableInput p_input,
                        String p_name,
                        String p_value,
                        boolean p_isSelected)
    {
        assertEquals(p_name, p_input.getName());
        assertEquals(p_value, p_input.getValue());
        assertEquals(p_isSelected, p_input.isSelected());
    }
}
