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

import junit.framework.TestCase;

import org.jamon.Escaping;

public class HtmlEscapingTest
    extends TestCase
{

    public void testEscaping()
        throws Exception
    {
        Writer writer = new StringWriter();
        Escaping.HTML.write("<>&\"'", writer);
        assertEquals("&lt;&gt;&amp;&#34;&#39;", writer.toString());
    }

}
