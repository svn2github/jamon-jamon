/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.templates;

import org.jamon.render.html.TextInput;

@Deprecated public class TextInputTest
    extends TestBase
{
    public void testSimple()
        throws Exception
    {
        TextInput input = new TextInput("foo", "bar");
        new org.jamon.html.TextInput(getTemplateManager())
            .render(getWriter(), input);
        checkOutput("<input type='text'\n       name='"
                    + input.getName()
                    + "'\n       value='"
                    + input.getValue()
                    +"'\n/>");
    }
}
