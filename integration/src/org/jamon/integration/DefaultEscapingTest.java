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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.integration;

import org.jamon.StaticTemplateManager;
import org.jamon.escaping.Escaping;
import test.jamon.Escapable;

public class DefaultEscapingTest
    extends TestBase
{
    public void testDefaultDefault()
        throws Exception
    {
        new Escapable(new StaticTemplateManager()).render(getWriter());
        checkOutput("it's \"quoted\" &amp;");
    }

    public void testHtmlDefault()
        throws Exception
    {
        new Escapable(new StaticTemplateManager(Escaping.HTML))
            .render(getWriter());
        checkOutput("it's \"quoted\" &amp;");
    }

    public void testUrlDefault()
        throws Exception
    {
        new Escapable(new StaticTemplateManager(Escaping.URL))
            .render(getWriter());
        checkOutput("it%27s+%22quoted%22+%26");
    }
}
