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
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.integration;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import org.jamon.Escaping;

import test.jamon.Encodings;

/**
 * Test Jamon's encoding mechanisms.
 **/

public class EncodingsTest
    extends TestBase
{
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
        Encodings encodings = new Encodings(getTemplateManager())
            .writeTo(getWriter());
        if (p_escaping == null)
        {
            p_escaping = Escaping.DEFAULT; // Ooo! Feels kinda naughty.
        }
        else
        {
            // Only set escaping when p_escaping is non-null
            encodings.escaping(p_escaping);
        }
        encodings.render();
        checkOutput("Escaping is " + p_escaping, escapedExpected(p_escaping));
    }

    private String escapedExpected(Escaping p_escaping)
        throws IOException
    {
        Writer writer = new StringWriter();
        // write it three times because that is what the template does
        p_escaping.write(TEMPLATE_TEXT, writer);
        p_escaping.write(TEMPLATE_TEXT, writer);
        p_escaping.write(TEMPLATE_TEXT, writer);
        return writer.toString();
    }

    private static final String TEMPLATE_TEXT = "<>&\"'";

}
