/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.templates;

import org.jamon.render.html.SelectableInput;
import org.jamon.html.Checkbox;

@Deprecated public class CheckboxTest
    extends TestBase
{
    public void testSimple()
        throws Exception
    {
        SelectableInput input = new SelectableInput("foo", "bar");
        new org.jamon.html.Checkbox(getTemplateManager())
            .render(getWriter(), input);
        checkOutput("<input type='checkbox'\n       name='"
                    + input.getName()
                    + "'\n       value='"
                    + input.getValue()
                    +"'\n/>");
    }

    public void testSimple2()
        throws Exception
    {
        SelectableInput input = new SelectableInput("foo", "bar", true);
        new org.jamon.html.Checkbox(getTemplateManager())
            .render(getWriter(), input);
        checkOutput("<input type='checkbox'\n       name='"
                    + input.getName()
                    + "'\n       value='"
                    + input.getValue()
                    + "'\n       checked"
                    + "\n/>");
    }


    public void testWithOptions()
        throws Exception
    {
        SelectableInput input = new SelectableInput("foo", "bar");
        Checkbox template = new Checkbox(getTemplateManager());
        template
            .setCssClass("x")
            .setSelected(Boolean.TRUE);
        template.render(getWriter(), input);
        checkOutput("<input type='checkbox'\n       name='"
                    + input.getName()
                    + "'\n       value='"
                    + input.getValue()
                    + "'\n       checked"
                    + "\n       class='"
                    + "x'\n/>");
    }
}
