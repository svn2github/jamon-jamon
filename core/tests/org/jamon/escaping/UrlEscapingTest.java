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

package org.jamon.escaping;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;
import java.net.URLEncoder;

import junit.framework.TestCase;

public class UrlEscapingTest
    extends TestCase
{
    public void testAscii() throws IOException
    {
        for (char c = 0; c <= 0x7f; c++)
        {
            check(Character.toString(c));
        }
    }

    public void testNonAscii() throws IOException
    {
        for(int i = 8; i <= 16; i++)
        {
            check(Character.toString((char) (1 << (i - 1))));
        }
        for(int i = 17; i <= 32; i++)
        {
            check(Character.toString((char) (1 << (i - 17))) + '\u0000');
        }
    }

    public void testUtf16() throws IOException
    {
        check("\ud800\udf02");
        check("\udf02");
        check("\udf02\u0000");
        check("\ud800\u0000");
        check("\ud800");
    }

    private void check(String p_text)
        throws IOException
    {
        Writer writer = new StringWriter();
        Escaping.URL.write(p_text, writer);
        assertEquals((Object) URLEncoder.encode(p_text, "UTF-8"),
                     writer.toString());
    }
}
