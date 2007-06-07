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
