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
            if( Character.isLetterOrDigit(c)
                || c == '.'
                || c == '-'
                || c == '*'
                || c == '_')
            {
                check(Character.toString(c), Character.toString(c));
            }
            else if (c == ' ')
            {
                check(Character.toString(c), "+");
            }
            else {
                check(Character.toString(c),
                      "%"
                      + ((c < 0x10) ? "0" : "")
                      + Integer.toHexString(c).toUpperCase());
            }
        }
    }

    public void testNonAscii() throws IOException
    {
        check("\u0080", "%C2%80");
        check("\u0100", "%C4%80");
        check("\u0200", "%C8%80");
        check("\u0400", "%D0%80");
        check("\u0800", "%E0%A0%80");
        check("\u1000", "%E1%80%80");
        check("\u2000", "%E2%80%80");
        check("\u4000", "%E4%80%80");
        check("\u8000", "%E8%80%80");
    }

    public void testUtf16SurrogatesInLine() throws IOException
    {
        check("q\ud800\udc00r", "q%F0%90%80%80r");
    }


    public void testUtf16SurrogateBits() throws IOException
    {
        check("\ud800\udc00", "%F0%90%80%80");
        check("\ud800\udc01", "%F0%90%80%81");
        check("\ud800\udc02", "%F0%90%80%82");
        check("\ud800\udc04", "%F0%90%80%84");
        check("\ud800\udc08", "%F0%90%80%88");
        check("\ud800\udc10", "%F0%90%80%90");
        check("\ud800\udc20", "%F0%90%80%A0");
        check("\ud800\udc40", "%F0%90%81%80");
        check("\ud800\udc80", "%F0%90%82%80");
        check("\ud800\udd00", "%F0%90%84%80");
        check("\ud800\ude00", "%F0%90%88%80");
        check("\ud801\udc00", "%F0%90%90%80");
        check("\ud802\udc00", "%F0%90%A0%80");
        check("\ud804\udc00", "%F0%91%80%80");
        check("\ud808\udc00", "%F0%92%80%80");
        check("\ud810\udc00", "%F0%94%80%80");
        check("\ud820\udc00", "%F0%98%80%80");
        check("\ud840\udc00", "%F0%A0%80%80");
        check("\ud880\udc00", "%F0%B0%80%80");
        check("\ud900\udc00", "%F1%90%80%80");
        check("\udA00\udc00", "%F2%90%80%80");
    }

    public void testUtf16BadSurrogates() throws IOException
    {
        check("\udf02\u0000", "%3F%00"); // starting with a low surrogate
        check("\udf02", "%3F"); // starting and ending with a low surrogate
        check("\ud800\u0000", "%3F%00"); // bad low surrogate
        check("\ud800", ""); // ending with a high surrogate
    }

    private void check(String p_text, String p_expected)
        throws IOException
    {
        Writer writer = new StringWriter();
        Escaping.URL.write(p_text, writer);
        assertEquals((Object) p_expected, writer.toString());
    }
}
