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
 * The Initial Developer of the Original Code is Jay Sachs. Portions
 * created by Jay Sachs are Copyright (C) 2003 Luis O'Shea.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.integration;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import org.jamon.escaping.Escaping;

import test.jamon.Sticky;

/**
 * Test that setting the escaping is "sticky", i.e. persists between
 * invocations of render().
 **/

public class StickyEscapingTest
    extends TestBase
{
    protected int cacheSize()
    {
        return 0; // disable caching - make sure we get a new instance
    }

    public void testDefault()
        throws Exception
    {
        checkEscaping(null);
    }

    public void testNone()
        throws IOException
    {
        checkEscaping(Escaping.NONE);
    }

    public void testHtml()
        throws IOException
    {
        checkEscaping(Escaping.HTML);
    }

    public void testStrictHtml()
        throws IOException
    {
        checkEscaping(Escaping.STRICT_HTML);
    }

    public void testUrl()
        throws IOException
    {
        checkEscaping(Escaping.URL);
    }

    public void testXml()
        throws IOException
    {
        checkEscaping(Escaping.XML);
    }

    private void checkEscaping(Escaping p_escaping)
        throws IOException
    {
        Sticky template = new Sticky(getTemplateManager())
            .writeTo(getWriter());
        if (p_escaping != null)
        {
            template.escaping(p_escaping);
        }

        template.render();
        String first = getWriter().toString();
        resetWriter();
        template.writeTo(getWriter());
        template.render();
        assertEquals("Escaping is " + p_escaping,
                     first,
                     getWriter().toString());
    }
}
