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

package org.jamon.templates.html;

import org.jamon.render.html.SelectableInput;
import org.jamon.html.Checkbox;

public class CheckboxTest
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
