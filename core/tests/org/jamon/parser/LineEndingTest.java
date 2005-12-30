package org.jamon.parser;

import org.jamon.node.JavaLineNode;
import org.jamon.node.TextNode;

public class LineEndingTest extends AbstractParserTest
{
    public void testJavaLineForUnix() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(new JavaLineNode(location(1,1), " java\n"))
                .addSubNode(new TextNode(location(2,1), "text")),
            parse("% java\ntext"));
    }

    public void testJavaLineForWindows() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(new JavaLineNode(location(1,1), " java\r\n"))
                .addSubNode(new TextNode(location(2,1), "text")),
            parse("% java\r\ntext"));
    }

    public void testJavaLineForMac() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(new JavaLineNode(location(1,1), " java\r"))
                .addSubNode(new TextNode(location(2,1), "text")),
            parse("% java\rtext"));
    }
}
