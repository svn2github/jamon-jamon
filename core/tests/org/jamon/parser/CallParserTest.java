package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserErrors;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.AbstractCallNode;
import org.jamon.node.AbstractNode;
import org.jamon.node.ChildCallNode;
import org.jamon.node.FragmentCallNode;
import org.jamon.node.MultiFragmentCallNode;
import org.jamon.node.NamedFragmentNode;
import org.jamon.node.NamedParamNode;
import org.jamon.node.NamedParamsNode;
import org.jamon.node.NoParamsNode;
import org.jamon.node.ParamNameNode;
import org.jamon.node.ParamValueNode;
import org.jamon.node.PathElementNode;
import org.jamon.node.RelativePathNode;
import org.jamon.node.SimpleCallNode;
import org.jamon.node.TextNode;
import org.jamon.node.UnnamedFragmentNode;
import org.jamon.node.UnnamedParamsNode;

/**
 * @author ian
 **/
public class CallParserTest extends AbstractParserTest
{
    public CallParserTest(String p_name)
    {
        super(p_name);
    }

    protected AbstractNode parse(String p_text) throws IOException
    {
        final PositionalPushbackReader reader = makeReader(p_text);
        assertEquals('<', reader.read());
        assertEquals('&', reader.read());
        ParserErrors errors = new ParserErrors();
        AbstractCallNode result =
            new CallParser(reader, errors, START_LOC).getCallNode();
        if (errors.hasErrors())
        {
            throw errors;
        }
        else
        {
            return result;
        }
    }

    public void testParseBasicCall() throws Exception
    {
        assertEquals(
            new SimpleCallNode(
                START_LOC,
                new RelativePathNode(location(1, 4)).addPathElement(
                    new PathElementNode(location(1, 4), "foo")),
                new NoParamsNode(location(1, 8))),
            parse("<& foo &>"));
    }

    public void testParseNamedArgCall() throws Exception
    {
        assertEquals(
            new SimpleCallNode(
                START_LOC,
                new AbsolutePathNode(location(1, 4)).addPathElement(
                    new PathElementNode(location(1, 5), "foo")),
                new NamedParamsNode(location(1, 8))),
            parse("<& /foo; &>"));
        assertEquals(
            new SimpleCallNode(
                START_LOC,
                new AbsolutePathNode(location(1, 4)).addPathElement(
                    new PathElementNode(location(1, 5), "foo")),
                new NamedParamsNode(location(1, 8)).addParam(
                    new NamedParamNode(
                        location(1, 10),
                        new ParamNameNode(location(1, 10), "a"),
                        new ParamValueNode(location(1, 15), "\"a;\" ")))),
            parse("<& /foo; a => \"a;\" &>"));
        assertEquals(
            new SimpleCallNode(
                START_LOC,
                new AbsolutePathNode(location(1, 4)).addPathElement(
                    new PathElementNode(location(1, 5), "foo")),
                new NamedParamsNode(location(1, 8)).addParam(
                    new NamedParamNode(
                        location(1, 10),
                        new ParamNameNode(location(1, 10), "a"),
                        new ParamValueNode(location(1, 15), "\"a;\"")))),
            parse("<& /foo; a => \"a;\"; &>"));
        assertEquals(
            new SimpleCallNode(
                START_LOC,
                new AbsolutePathNode(location(1, 4)).addPathElement(
                    new PathElementNode(location(1, 5), "foo")),
                new NamedParamsNode(location(1, 8))
                    .addParam(
                        new NamedParamNode(
                            location(1, 10),
                            new ParamNameNode(location(1, 10), "a"),
                            new ParamValueNode(location(1, 15), "\"a;\"")))
                    .addParam(
                        new NamedParamNode(
                            location(1, 21),
                            new ParamNameNode(location(1, 21), "b"),
                            new ParamValueNode(location(1, 26), "'&' ")))),
            parse("<& /foo; a => \"a;\"; b => '&' &>"));
    }

    public void testParseUnnamedArgCall() throws Exception
    {
        assertEquals(
            new SimpleCallNode(
                START_LOC,
                new AbsolutePathNode(location(1, 4)).addPathElement(
                    new PathElementNode(location(1, 5), "foo")),
                new UnnamedParamsNode(location(1, 8))),
            parse("<& /foo: &>"));
        assertEquals(
            new SimpleCallNode(
                START_LOC,
                new AbsolutePathNode(location(1, 4)).addPathElement(
                    new PathElementNode(location(1, 5), "foo")),
                new UnnamedParamsNode(location(1, 8)).addValue(
                    new ParamValueNode(location(1, 10), "\"a;\" "))),
            parse("<& /foo: \"a;\" &>"));
        assertEquals(
            new SimpleCallNode(
                START_LOC,
                new AbsolutePathNode(location(1, 4)).addPathElement(
                    new PathElementNode(location(1, 5), "foo")),
                new UnnamedParamsNode(location(1, 8)).addValue(
                    new ParamValueNode(location(1, 10), "\"a;\""))),
            parse("<& /foo: \"a;\"; &>"));
        assertEquals(
            new SimpleCallNode(
                START_LOC,
                new AbsolutePathNode(location(1, 4)).addPathElement(
                    new PathElementNode(location(1, 5), "foo")),
                new UnnamedParamsNode(location(1, 8))
                    .addValue(new ParamValueNode(location(1, 10), "\"a;\""))
                    .addValue(new ParamValueNode(location(1, 16), "'&' "))),
            parse("<& /foo: \"a;\"; '&' &>"));
        assertEquals(
            new SimpleCallNode(
                START_LOC,
                new AbsolutePathNode(location(1, 4)).addPathElement(
                    new PathElementNode(location(1, 5), "foo")),
                new UnnamedParamsNode(location(1, 8))
                    .addValue(
                        new ParamValueNode(location(1, 10), "x && a > b "))),
            parse("<& /foo: x && a > b &>"));
    }
    
    public void testChildCall() throws IOException
    {
        String call = "<& *CHILD &>";
        assertEquals(new ChildCallNode(START_LOC), parse(call));    
    }

    public void testFragmentCall() throws Exception
    {
        assertEquals(
            new FragmentCallNode(
                START_LOC,
                new AbsolutePathNode(location(1, 5)).addPathElement(
                    new PathElementNode(location(1, 6), "foo")),
                new UnnamedParamsNode(location(1, 9)).addValue(
                    new ParamValueNode(location(1, 11), "3 ")),
                (UnnamedFragmentNode) new UnnamedFragmentNode(
                    location(1, 15)).addSubNode(
                    new TextNode(location(1, 15), "bar"))),
            parse("<&| /foo: 3 &>bar</&>"));
    }

    private static String MULTI_FRAGMENT_START = "<&|| foo &>\n";
    private static String FRAGMENTS_END = "</&>";
    private static MultiFragmentCallNode makeMultiFragmentCall()
    {
        return new MultiFragmentCallNode(
            START_LOC,
            new RelativePathNode(location(1, 6)).addPathElement(
                new PathElementNode(location(1, 6), "foo")),
            new NoParamsNode(location(1, 10)));
    }

    public void testMultiFragmentCall() throws Exception
    {
        assertEquals(
            makeMultiFragmentCall(),
            parse(MULTI_FRAGMENT_START + FRAGMENTS_END));
        assertEquals(
            makeMultiFragmentCall().addFragment(
                (NamedFragmentNode) new NamedFragmentNode(
                    location(2, 1),
                    "bar").addSubNode(
                    new TextNode(location(2, 7), "baz"))),
            parse(MULTI_FRAGMENT_START + "<|bar>baz</|>" + FRAGMENTS_END));
        assertEquals(
            makeMultiFragmentCall()
                .addFragment(
                    (NamedFragmentNode) new NamedFragmentNode(
                        location(2, 1),
                        "bar").addSubNode(
                        new TextNode(location(2, 7), "baz")))
                .addFragment(
                    (NamedFragmentNode) new NamedFragmentNode(
                        location(3, 1),
                        "bob").addSubNode(
                        new TextNode(location(3, 7), "joe"))),
            parse(
                MULTI_FRAGMENT_START
                    + "<|bar>baz</|>\n<|bob>joe</|>"
                    + FRAGMENTS_END));
    }
    
    public void testMaformedCallTag() throws Exception
    {
        assertError("<&foo; a b", 1, 10, CallParser.MISSING_ARG_ARROW_ERROR);
        assertError("<& foo a", 1, 8, CallParser.GENERIC_ERROR);
        assertError(
            "<&| foo &>bob",
            1,
            11,
            UnamedFragmentParser.FRAGMENT_CLOSE_EXPECTED);
        assertError("<& foo:", 1, 8, CallParser.PARAM_VALUE_EOF_ERROR);
        assertError(
            "<&|| foo &><|foo></|>",
            1,
            12,
            CallParser.FRAGMENTS_EOF_ERROR);
        assertErrorPair(
            "<&|| foo &>\n<|bar>123",
            2,
            7,
            NamedFragmentParser.NAMED_FRAGMENT_CLOSE_EXPECTED,
            1,
            12,
            CallParser.FRAGMENTS_EOF_ERROR);
        assertError(
            "<&|| foo &>\nbob",
            2,
            1,
            CallParser.UNEXPECTED_IN_MULTI_FRAG_ERROR);
        assertError(
            "<&|| foo &>\n<bob",
            2,
            2,
            CallParser.UNEXPECTED_IN_MULTI_FRAG_ERROR);
        assertError(
            "<&|| foo &>\n<",
            2,
            2,
            CallParser.UNEXPECTED_IN_MULTI_FRAG_ERROR);
        assertError(
            "<&|| foo &>\n<|bob",
            2,
            5,
            CallParser.UNEXPECTED_IN_MULTI_FRAG_ERROR);
        assertError(
            "<&|| foo &>\n<|></|></&>",
            2,
            3,
            AbstractParser.NOT_AN_IDENTIFIER_ERROR);
    }

    public void testAsterixNonChildCall() throws Exception
    {
        assertError(
            "<& *NOTCHILD &>", 1, 4, CallParser.INVALID_CALL_TARGET_ERROR);
    }
    
    public void testMalformedChildCall() throws Exception
    {
        assertError(
            "<& *CHILD foo", 1, 11, CallParser.MISSING_CALL_CLOSE_ERROR);
    }
}
