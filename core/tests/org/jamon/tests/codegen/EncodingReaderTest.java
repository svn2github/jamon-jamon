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

package org.jamon.tests.codegen;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.jamon.codegen.EncodingReader;

public class EncodingReaderTest
    extends TestCase
{
    public void testLatin1()
        throws Exception
    {
        doTest("latin1", "abcdefg12345!@#$%^\u00B2\u00EC");
        doTest("ISO8859-1", "abcdefg12345!@#$%^\u00B2\u00EC");
    }

    public void testUtf8()
        throws Exception
    {
        doTest("utf-8", "abcdefg12345!@#$%^\u00B2\u00EC\u3092");
    }

    public void testUtf16()
        throws Exception
    {
        doTest("utf-16", "abcdefg12345!@#$%^\u00B2\u00EC\u3092");
    }

    private void doTest(final String p_encoding, final String p_stuff)
        throws Exception
    {
        StringWriter writer = new StringWriter();
        writer.write("<%encoding \t ");
        writer.write(p_encoding);
        writer.write("  >    \n\t  \n");
        writer.write(p_stuff);
        EncodingReader reader =
            new EncodingReader(new ByteArrayInputStream
                               (writer.toString().getBytes(p_encoding)));
        assertEquals(p_stuff, readAll(reader));
    }

    private String readAll(Reader p_reader)
        throws IOException
    {
        BufferedReader reader = new BufferedReader(p_reader);
        StringBuffer buf = new StringBuffer();
        String s = null;
        while ((s = reader.readLine()) != null)
        {
            buf.append(s);
        }
        return buf.toString();
    }
}
