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
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.parser;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

import org.jamon.TemplateFileLocation;
import org.jamon.TemplateLocation;
import org.jamon.node.Location;

public class PositionalPushbackReaderTest extends TestCase
{
    public PositionalPushbackReaderTest(String p_name)
    {
        super(p_name);
    }

    private static TemplateLocation TEMPLATE_LOC =
        new TemplateFileLocation("x");

    private static Location location(int p_line, int p_column)
    {
        return new Location(TEMPLATE_LOC, p_line, p_column);
    }

    public void testUnixLinefeeds() throws Exception
    {
        checkLocations("ab\ncd", 1,1, 1,2, 1,3, 2,1, 2,2);
    }

    public void testWindowsLineFeeds() throws Exception
    {
        checkLocations("ab\r\ncd", 1,1, 1,2, 1,3, 1,4, 2,1, 2,2);
    }

    public void testMacLineFeeds() throws Exception
    {
        // OS 9 is (presumed) dead
        checkLocations("ab\rcd", 1,1, 1,2, 1,3, 1,4, 1,5);
    }

    private void checkLocations(final String p_text, final int... p_locations)
        throws IOException
    {
        assertTrue(p_locations.length % 2 == 0);
        Location[] locations = new Location[p_locations.length /2];
        for (int i = 0; i < locations.length; i++)
        {
            locations[i] = location(p_locations[i * 2], p_locations[i * 2 + 1]);
        }
        checkRead(p_text, locations);
        checkUnread(p_text, locations);
        checkIsLineStart(p_text);
    }

    private void checkRead(final String p_text, final Location[] p_locations)
        throws IOException
    {
        PositionalPushbackReader reader = makeReader(p_text);
        for (int i = 0; i < p_text.length(); i++)
        {
            assertEquals(p_locations[i], reader.getNextLocation());
            assertEquals(p_text.charAt(i), reader.read());
            assertEquals(p_locations[i], reader.getLocation());
        }
    }

    private void checkUnread(final String p_text, final Location[] p_locations)
        throws IOException
    {
        for (int pushbackSize = 1; pushbackSize <= 3; pushbackSize++)
        {
            PositionalPushbackReader reader = makeReader(p_text, pushbackSize);
            for (int i = 0; i < p_text.length() - pushbackSize; i++)
            {
                assertEquals(p_locations[i], reader.getNextLocation());
                for (int j = 0; j <= pushbackSize; j++)
                {
                    assertEquals(p_text.charAt(i + j), reader.read());
                }
                for (int j = pushbackSize; j > 0; j--)
                {
                    reader.unread(p_text.charAt(i + j));
                }
                assertEquals(p_locations[i], reader.getLocation());
            }
        }
    }

    private void checkIsLineStart(final String p_text) throws IOException
    {
        PositionalPushbackReader reader = makeReader(p_text);
        for (int i = 0; i < p_text.length(); i++)
        {
            assertEquals(p_text.charAt(i), reader.read());
            assertEquals(
                reader.getLocation().getColumn() == 1,
                reader.isLineStart());
        }
    }

    public void testMarkNodeBeginning() throws Exception
    {
        PositionalPushbackReader reader = makeReader("12345");
        reader.read();
        reader.read();
        reader.markNodeBeginning();
        assertEquals(location(1, 2), reader.getCurrentNodeLocation());
        while (reader.read() >= 0)
        {
            assertEquals(location(1, 2), reader.getCurrentNodeLocation());
        }
    }

    public void testMarkNodeEnd() throws Exception
    {
        PositionalPushbackReader reader = makeReader("12345");
        reader.read();
        reader.read();
        reader.markNodeEnd();
        assertEquals(location(1, 3), reader.getCurrentNodeLocation());
        while (reader.read() >= 0)
        {
            assertEquals(location(1, 3), reader.getCurrentNodeLocation());
        }
    }

    public void testNodePosition() throws Exception
    {
        assertEquals(
            location(1, 1),
            makeReader("").getCurrentNodeLocation());
    }

    public void testPushbackEof() throws Exception
    {
        PositionalPushbackReader reader = makeReader("123");
        int c;
        while ((c = reader.read()) >= 0);
        reader.unread(c);
        assertEquals(-1, reader.read());
    }

    private PositionalPushbackReader makeReader(
        String p_text, int p_pushbackSize)
    {
        return new PositionalPushbackReader(
            TEMPLATE_LOC, new StringReader(p_text), p_pushbackSize);
    }


    private PositionalPushbackReader makeReader(String p_text)
    {
        return makeReader(p_text, 1);
    }
}
