/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.render.html;

import junit.framework.TestCase;

@Deprecated public class InputTest
    extends TestCase
{

    public void testDefaultValue()
    {
        final String NAME = "foo";
        Input input = new Input(NAME);
        assertEquals(NAME, input.getName());
        assertEquals(null, input.getValue());
    }

    public void testSpecifiedValue()
    {
        final String NAME = "foo";
        final String VALUE = "bar";
        Input input = new Input(NAME, VALUE);
        assertEquals(NAME, input.getName());
        assertEquals(VALUE, input.getValue());
    }
}
