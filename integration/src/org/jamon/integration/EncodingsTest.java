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

package org.jamon.integration;

/**
 * Test Jamon's encoding mechanisms.
 **/

public class EncodingsTest
    extends TestBase
{
    public void testUtf8()
        throws Exception
    {
        new test.jamon.Utf8().render(getWriter());
        checkOutput("Onc\u00e9\u00f2\u00e1\n\u00e9\u00c1\u00f3\u00e9\n");
    }

    public void testLatin1()
        throws Exception
    {
        new test.jamon.Latin1().render(getWriter());
        checkOutput("Onc\u00e9\u00f2\u00e1\n\u00e9\u00c1\u00f3\u00e9\n");
    }

    public void testMalformedEncoding1()
        throws Exception
    {
        expectTemplateException("BadEncoding",
                                "Malformed encoding tag; expected '>'",
                                1,
                                16);
    }

    public void testMalformedEncoding2()
        throws Exception
    {
        expectTemplateException("BadEncoding2",
                                "EOF before encoding tag finished",
                                1,
                                12);
    }

    public void testMalformedEncoding3()
        throws Exception
    {
        try
        {
            generateSource("test/jamon/broken/BadEncoding3");
            fail();
        }
        catch(java.io.UnsupportedEncodingException e)
        {
            // expected
        }
    }

}
