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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Luis O'Shea.  Portions
 * created by Luis O'Shea are Copyright (C) 2002 Luis O'Shea.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.tests;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.jamon.Escaping;

public class EscapingTest
    extends TestCase
{

    public void testHtmlEscaping()
        throws IOException
    {
        check(Escaping.HTML, "&lt;&gt;&amp;\"'");
    }

    public void testStrictHtmlEscaping()
        throws IOException
    {
        check(Escaping.STRICT_HTML, "&lt;&gt;&amp;&#34;&#39;");
    }

    private void check(Escaping p_escaping, String p_expected)
        throws IOException
    {
        Writer writer = new StringWriter();
        p_escaping.write("<>&\"'", writer);
        assertEquals(p_expected, writer.toString());
    }

}
