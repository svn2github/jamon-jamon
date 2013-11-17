/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.render.html;

import junit.framework.TestCase;

@Deprecated public class TextInputTest
    extends TestCase
{
    public void testConstruction4()
    {
        final String NAME = "foo";
        final String VALUE = "bar";
        final int MAXLEN = 7;
        verify(new TextInput(NAME, VALUE, MAXLEN, false),
               NAME,
               VALUE,
               MAXLEN,
               false);
        verify(new TextInput(NAME, VALUE, MAXLEN, true),
               NAME,
               VALUE,
               MAXLEN,
               true);
    }


    public void testConstruction3()
    {
        final String NAME = "foo";
        final String VALUE = "bar";
        final int MAXLEN = 7;
        verify(new TextInput(NAME, VALUE, MAXLEN),
               NAME,
               VALUE,
               MAXLEN,
               false);
    }

    public void testConstruction2()
    {
        final String NAME = "foo";
        final String VALUE = "bar";
        verify(new TextInput(NAME, VALUE),
               NAME,
               VALUE,
               0,
               false);
    }

    public void testConstruction1()
    {
        final String NAME = "foo";
        verify(new TextInput(NAME),
               NAME,
               null,
               0,
               false);
    }

    private void verify(TextInput p_input,
                        String p_name,
                        String p_value,
                        int p_maxlen,
                        boolean p_uconly)
    {
        assertEquals(p_name, p_input.getName());
        assertEquals(p_value, p_input.getValue());
        assertEquals(p_maxlen, p_input.getMaxLength());
        assertEquals(p_uconly, p_input.isUpperCaseOnly());
    }
}
