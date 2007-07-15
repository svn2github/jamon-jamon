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

    @Test public void testBackslashForUnix() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(new TextNode(location(1,1), "textmore")),
            parse("text\\\nmore"));
    }

    @Test public void testNonNewlineBackslash() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(new TextNode(location(1,1), "text\\\rmore")),
            parse("text\\\rmore"));
        assertEquals(
            topNode()
                .addSubNode(new TextNode(location(1,1), "text\\more")),
            parse("text\\more"));
        assertEquals(
            topNode()
                .addSubNode(new TextNode(location(1,1), "text\\")),
            parse("text\\"));
    }

    @Test public void testBackslashForWindows() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(new TextNode(location(1,1), "textmore")),
            parse("text\\\r\nmore"));
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
