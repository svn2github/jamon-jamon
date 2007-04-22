package org.jamon.parser;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jamon.ParserErrorsImpl;
import org.jamon.node.AbstractNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgValueNode;
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
        ParserErrorsImpl errors = new ParserErrorsImpl();
        ParentArgsNode result = new ParentArgsParser(reader, errors, START_LOC).getParentArgsNode();
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
        org.jamon.api.Location location = location(p_line, p_column);
        return new ParentArgNode(location, new ArgNameNode(location, p_name));
    }

    private static ParentArgNode parentArgWithDefaultNode(
        int p_nameLine, int p_nameColumn, String p_name,
        int p_valueLine, int p_valueColumn, String p_value) {
        org.jamon.api.Location location = location(p_nameLine, p_nameColumn);
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
        assertError(
            PARENT_ARGS_START + "x=3" + PARENT_ARGS_END,
            1, 4, AbstractParser.eofErrorMessage(";"));
    }

    @Test public void testXargWithSimpleType() throws Exception {
        assertError(
            PARENT_ARGS_START + "a c;" + PARENT_ARGS_END,
            1, 4, OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
    }

    @Test public void testXargWithFullyScopedType() throws Exception {
        assertError(
            PARENT_ARGS_START + "a.b c;" + PARENT_ARGS_END,
            1, 3, OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.JUnit4TestAdapter(ParentArgsParserTest.class);
    }
}
