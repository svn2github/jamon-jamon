package org.jamon.parser;

import static org.junit.Assert.*;

import org.jamon.node.JavaNode;
import org.jamon.node.TextNode;
import org.junit.Test;

public class LineEndingTest extends AbstractParserTest
{
    @Test public void testJavaLineForUnix() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(new JavaNode(location(1,1), " java\n"))
                .addSubNode(new TextNode(location(2,1), "text")),
            parse("% java\ntext"));
    }

    @Test public void testJavaLineForWindows() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(new JavaNode(location(1,1), " java\rmore java\r\n"))
                .addSubNode(new TextNode(location(2,1), "text")),
            parse("% java\rmore java\r\ntext"));
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.JUnit4TestAdapter(LineEndingTest.class);
    }
}
