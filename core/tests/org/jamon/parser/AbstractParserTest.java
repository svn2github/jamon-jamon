package org.jamon.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.TemplateFileLocation;
import org.jamon.TemplateLocation;
import org.jamon.node.AbstractNode;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.Location;
import org.jamon.node.PathElementNode;
import org.jamon.node.TopNode;
import org.jamon.node.UpdirNode;

/**
 * @author ian
 **/
public abstract class AbstractParserTest extends TestCase
{
    protected final static TemplateLocation TEMPLATE_LOC =
        new TemplateFileLocation("x");
    protected final static Location START_LOC =
        new Location(TEMPLATE_LOC, 1, 1);

    public AbstractParserTest() {}

    public AbstractParserTest(String p_name)
    {
        super(p_name);
    }

    protected static PositionalPushbackReader makeReader(String p_text)
    {
        return new PositionalPushbackReader(
            TEMPLATE_LOC, new StringReader(p_text));
    }

    protected static TopNode topNode()
    {
        return new TopNode(new Location(TEMPLATE_LOC, 1, 1));
    }

    protected static Location location(int p_line, int p_column)
    {
        return new Location(TEMPLATE_LOC, p_line, p_column);
    }

    protected AbstractNode parse(String p_text) throws IOException
    {
        return new TopLevelParser(TEMPLATE_LOC, new StringReader(p_text))
            .parse()
            .getRootNode();
    }


    private void assertParserError(Iterator<ParserError> p_errors,
                                   int p_line, int p_column,
                                   String p_message)
        throws Exception
    {
        assertTrue(p_errors.hasNext());
        assertEquals(
            new ParserError(new Location(TEMPLATE_LOC, p_line, p_column),
                            p_message),
            p_errors.next());
    }

    private void assertNoMoreErrors(Iterator<ParserError> p_errors)
    {
        if (p_errors.hasNext())
        {
            fail("More errors still: " + p_errors.next());
        }
    }

    protected void assertError(
        String p_body,
        int p_line, int p_column, String p_message)
        throws Exception
    {
        try
        {
            parse(p_body);
            fail("No failure registered for '" + p_body + "'");
        }
        catch (ParserErrors e)
        {
            Iterator<ParserError> iter = e.getErrors();
            assertParserError(iter, p_line, p_column, p_message);
            assertNoMoreErrors(iter);
        }
    }

    protected void assertErrorPair(
            String p_body,
            int p_line1, int p_column1, String p_message1,
            int p_line2, int p_column2, String p_message2)
            throws Exception
    {
        try
        {
           parse(p_body);
           fail("No failure registered for '" + p_body + "'");
        }
        catch (ParserErrors e)
        {
            Iterator<ParserError> iter = e.getErrors();
            assertParserError(iter, p_line1, p_column1, p_message1);
            assertParserError(iter, p_line2, p_column2, p_message2);
            assertNoMoreErrors(iter);
        }
    }

    protected void assertErrorTripple(
            String p_body,
            int p_line1, int p_column1, String p_message1,
            int p_line2, int p_column2, String p_message2,
            int p_line3, int p_column3, String p_message3)
            throws Exception
    {
        try
        {
           parse(p_body);
                fail("No failure registered for '" + p_body + "'");
        }
        catch (ParserErrors e)
        {
            Iterator<ParserError> iter = e.getErrors();
            assertParserError(iter, p_line1, p_column1, p_message1);
            assertParserError(iter, p_line2, p_column2, p_message2);
            assertParserError(iter, p_line3, p_column3, p_message3);
            assertNoMoreErrors(iter);
        }
    }

    protected static AbstractPathNode buildPath(Location p_start, AbstractPathNode p_path, String p_elements)
    {
        Location loc = p_start;
        StringTokenizer tokenizer = new StringTokenizer(p_elements, "/");
        while (tokenizer.hasMoreTokens())
        {
            String elt = tokenizer.nextToken();
            if ("..".equals(elt))
            {
                p_path.addPathElement(new UpdirNode(loc));
            }
            else
            {
                p_path.addPathElement(new PathElementNode(loc, elt));
            }
            loc =
                new Location(loc.getTemplateLocation(),
                             loc.getLine(),
                             loc.getColumn() + 1 + elt.length());
        }
        return p_path;
    }
}
