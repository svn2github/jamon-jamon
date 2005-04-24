package org.jamon.parser;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

import org.jamon.TemplateFileLocation;
import org.jamon.TemplateLocation;
import org.jamon.node.Location;

/**
 * @author ian
 **/
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
        final String text = "ab\ncd";
        final Location[] locations =
            {
                location(1, 1),
                location(1, 2),
                location(1, 3),
                location(2, 1),
                location(2, 2)};

        checkLocations(text, locations);
    }

    public void testWindowsLineFeeds() throws Exception
    {
        final String text = "ab\r\ncd";
        final Location[] locations =
            {
                location(1, 1),
                location(1, 2),
                location(1, 3),
                location(2, 1),
                location(2, 1),
                location(2, 2)};
        checkLocations(text, locations);
    }

    public void testMacLineFeeds() throws Exception
    {
        final String text = "ab\rcd";
        final Location[] locations =
            {
                location(1, 1),
                location(1, 2),
                location(1, 3),
                location(2, 1),
                location(2, 2)};
        checkLocations(text, locations);
    }

    private void checkLocations(
        final String p_text,
        final Location[] p_locations)
        throws IOException
    {
        checkRead(p_text, p_locations);
        checkUnread(p_text, p_locations);
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
        PositionalPushbackReader reader = makeReader(p_text);
        for (int i = 0; i < p_text.length() - 1; i++)
        {
            assertEquals(p_locations[i], reader.getNextLocation());
            assertEquals(p_text.charAt(i), reader.read());
            assertEquals(p_text.charAt(i + 1), reader.read());
            reader.unread(p_text.charAt(i + 1));
            assertEquals(p_locations[i], reader.getLocation());
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

    private PositionalPushbackReader makeReader(String p_text)
    {
        return new PositionalPushbackReader(
            TEMPLATE_LOC, new StringReader(p_text));
    }
}
