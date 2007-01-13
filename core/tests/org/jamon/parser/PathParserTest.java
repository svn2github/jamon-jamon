package org.jamon.parser;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jamon.ParserErrors;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.AbstractNode;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.Location;
import org.jamon.node.NamedAliasPathNode;
import org.jamon.node.RelativePathNode;
import org.jamon.node.RootAliasPathNode;
import org.junit.Test;

/**
 * @author ian
 **/
public class PathParserTest extends AbstractParserTest
{
    @Override protected AbstractNode parse(String p_text)
        throws IOException, ParserErrors
    {
        m_reader = makeReader(p_text);
        ParserErrors errors = new ParserErrors();
        AbstractNode node = new PathParser(m_reader, errors).getPathNode();
        if (errors.hasErrors())
        {
            throw errors;
        }
        return node;
    }

    private void checkPath(
        AbstractPathNode p_path,
        String p_pathString,
        int p_nextChar)
        throws IOException
    {
        assertEquals(p_path, parse(p_pathString));
        assertEquals(p_nextChar, m_reader.read());
    }

    @Test public void testRelativePath() throws Exception
    {
        checkPath(
            buildPath(START_LOC, new RelativePathNode(START_LOC), "foo"),
            "foo",
            -1);
        checkPath(
            buildPath(START_LOC, new RelativePathNode(START_LOC), "foo/bar"),
            "foo/bar",
            -1);
        checkPath(
            buildPath(
                START_LOC,
                new RelativePathNode(START_LOC),
                "foo/bar/baz"),
            "foo/bar/baz",
            -1);
        checkPath(
            buildPath(START_LOC, new RelativePathNode(START_LOC), "../foo"),
            "../foo",
            -1);
        checkPath(
            buildPath(START_LOC, new RelativePathNode(START_LOC), "../../foo"),
            "../../foo",
            -1);
        checkPath(
            buildPath(
                START_LOC,
                new RelativePathNode(START_LOC),
                "../../../foo"),
            "../../../foo",
            -1);
    }

    @Test public void testNonEofPaths() throws Exception
    {
        final String ends = " ,;&";
        for (int i = 0; i < ends.length(); i++)
        {
            checkPath(
                buildPath(START_LOC, new RelativePathNode(START_LOC), "foo"),
                "foo" + ends.charAt(i),
                ends.charAt(i));
            checkPath(
                buildPath(
                    START_LOC,
                    new RelativePathNode(START_LOC),
                    "foo/bar"),
                "foo/bar" + ends.charAt(i),
                ends.charAt(i));
        }
    }

    @Test public void testAbsolutePath() throws Exception
    {

        Location relStart = location(1, 2);
        checkPath(
            buildPath(relStart, new AbsolutePathNode(START_LOC), "foo"),
            "/foo",
            -1);
        checkPath(
            buildPath(relStart, new AbsolutePathNode(START_LOC), "foo/bar"),
            "/foo/bar",
            -1);
    }

    @Test public void testAliasPath() throws Exception
    {
        checkPath(
            buildPath(
                location(1, 3),
                new RootAliasPathNode(START_LOC),
                "foo/bar"),
            "//foo/bar",
            -1);
        checkPath(
            buildPath(
                location(1, 6),
                new NamedAliasPathNode(START_LOC, "baz"),
                "foo/bar"),
            "baz//foo/bar",
            -1);
    }

    @Test public void testErrors() throws Exception
    {
        final String message = PathParser.GENERIC_PATH_ERROR;
        assertError("", 1, 1, message);
        assertError("%", 1, 1, message);
        assertError("/foo/", 1, 1, message);
        assertError("/foo/bar/", 1, 1, message);
        assertError("///foo", 1, 3, message);
        assertError("//foo//bar", 1, 7, message);
        assertError("/../foo", 1, 2, message);
        assertError("foo/../bar", 1, 5, message);
        assertError("..", 1, 1, message);
        assertError("/", 1, 1, message);
        assertError("/ ", 1, 1, message);
    }

    private PositionalPushbackReader m_reader = null;

    public static junit.framework.Test suite()
    {
        return new junit.framework.JUnit4TestAdapter(PathParserTest.class);
    }
}
