package org.jamon.parser;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jamon.ParserErrors;
import org.jamon.node.AbstractNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgValueNode;
import org.jamon.node.Location;
import org.jamon.node.ParentArgNode;
import org.jamon.node.ParentArgWithDefaultNode;
import org.jamon.node.ParentArgsNode;
import org.junit.Test;

public class ParentArgsParserTest extends AbstractParserTest
{
    private static String PARENT_ARGS_START = ">";
    private static String PARENT_ARGS_END = "</%xargs>";

    @Override protected AbstractNode parse(String p_text) throws IOException
    {
        final PositionalPushbackReader reader = makeReader(p_text);
        ParserErrors errors = new ParserErrors();
        ParentArgsNode result =
            new ParentArgsParser(reader, errors, START_LOC).getParentArgsNode();
        if (errors.hasErrors())
        {
            throw errors;
        }
        else
        {
            return result;
        }
    }


    private static ParentArgsNode parentArgsNode()
    {
        return new ParentArgsNode(START_LOC);
    }

    private static ParentArgNode parentArgNode(int p_line, int p_column, String p_name) {
        Location location = location(p_line, p_column);
        return new ParentArgNode(location, new ArgNameNode(location, p_name));
    }

    private static ParentArgNode parentArgWithDefaultNode(
        int p_nameLine, int p_nameColumn, String p_name,
        int p_valueLine, int p_valueColumn, String p_value) {
        Location location = location(p_nameLine, p_nameColumn);
        return new ParentArgWithDefaultNode(
            location,
            new ArgNameNode(location, p_name),
            new ArgValueNode(location(p_valueLine, p_valueColumn), p_value));
    }

    @Test public void testNoXargs() throws Exception {
        assertEquals(parentArgsNode(), parse(PARENT_ARGS_START + PARENT_ARGS_END));
        assertEquals(parentArgsNode(), parse(PARENT_ARGS_START + " \t\r\n" + PARENT_ARGS_END));
    }

    @Test public void testSomeXargs() throws Exception {
        assertEquals(
            parentArgsNode().addArg(parentArgNode(1, 2, "foo")).addArg(parentArgNode(2, 1, "bar")),
            parse(PARENT_ARGS_START + "foo;\nbar ; " + PARENT_ARGS_END));
    }

    @Test public void testOptionalXarg() throws Exception {
        assertEquals(
            parentArgsNode().addArg(parentArgWithDefaultNode(1, 2, "x", 1, 4, "3")),
            parse(PARENT_ARGS_START + "x=3;" + PARENT_ARGS_END));
    }

    @Test public void testNoSemiAfterXarg() throws Exception {
        assertError(
            PARENT_ARGS_START + "x" + PARENT_ARGS_END,
            1, 3, OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
    }

    @Test public void testNoSemiAfterXargWithDefault() throws Exception {
        //parse(PARENT_ARGS_START + "x=3" + PARENT_ARGS_END);
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.JUnit4TestAdapter(ParentArgsParserTest.class);
    }
}
