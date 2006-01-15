package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.node.AbstractNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgNode;
import org.jamon.node.ArgTypeNode;
import org.jamon.node.ArgValueNode;
import org.jamon.node.ArgsNode;
import org.jamon.node.OptionalArgNode;

public class ArgsParserTest extends AbstractParserTest
{
    private static String ARGS_START = ">";
    private static String ARGS_END = "</%args>";

    @Override protected AbstractNode parse(String p_text) throws IOException
    {
        final PositionalPushbackReader reader = makeReader(p_text);
        ParserErrors errors = new ParserErrors();
        try
        {
            ArgsNode result =
                new ArgsParser(reader, errors, START_LOC).getArgsNode();
            if (errors.hasErrors())
            {
                throw errors;
            }
            else
            {
                return result;
            }
        }
        catch (ParserError e)
        {
            errors.addError(e);
            throw errors;
        }
    }


    private static ArgsNode argsNode()
    {
        return new ArgsNode(START_LOC);
    }

    public void testNoArgs() throws Exception
    {
        assertEquals(argsNode(),
                     parse(ARGS_START + "   " + ARGS_END));
    }

    public void testRequiredArgs() throws Exception
    {
        assertEquals(
            argsNode()
                .addArg(new ArgNode(
                    location(2, 1),
                    new ArgTypeNode(location(2, 1), "int"),
                    new ArgNameNode(location(2, 5), "i")))
                .addArg(new ArgNode(
                    location(3, 1),
                    new ArgTypeNode(location(3, 1), "String"),
                    new ArgNameNode(location(3, 8), "s"))),
            parse(ARGS_START + "\nint i;\nString s;\n" + ARGS_END));
    }

    public void testOldStyleOptionalArgs() throws Exception
    {
        assertEquals(
            argsNode()
                .addArg(
                    new OptionalArgNode(
                        location(2, 1),
                        new ArgTypeNode(location(2, 1), "int"),
                        new ArgNameNode(location(2, 5), "i"),
                        new ArgValueNode(location(2, 10), "3")))
                .addArg(
                    new OptionalArgNode(
                        location(3, 1),
                        new ArgTypeNode(location(3, 1), "String"),
                        new ArgNameNode(location(3, 8), "s"),
                        new ArgValueNode(location(3, 13), "\";\""))),
            parse(
                ARGS_START + "\nint i => 3;\nString s => \";\";\n" + ARGS_END));

    }

    public void testNewStyleOptionalArgs() throws Exception
    {
        assertEquals(
            argsNode()
                .addArg(
                    new OptionalArgNode(
                        location(2, 1),
                        new ArgTypeNode(location(2, 1), "int"),
                        new ArgNameNode(location(2, 5), "i"),
                        new ArgValueNode(location(2, 9), "3")))
                .addArg(
                    new OptionalArgNode(
                        location(3, 1),
                        new ArgTypeNode(location(3, 1), "String"),
                        new ArgNameNode(location(3, 8), "s"),
                        new ArgValueNode(location(3, 12), "\";\""))),
            parse(
                ARGS_START + "\nint i = 3;\nString s = \";\";\n" + ARGS_END));

    }

    public void testFullyQualifiedClassNames() throws Exception
    {
        assertEquals(
            argsNode().addArg(new ArgNode(
                location(2,1),
                new ArgTypeNode(location(2,1), "foo.bar"),
                new ArgNameNode(location(2,9), "x"))),
            parse(ARGS_START + "\nfoo.bar x;\n"  + ARGS_END));
    }

    public void testArrays() throws Exception
    {
        assertEquals(
            argsNode().addArg(new ArgNode(location(2,1),
                              new ArgTypeNode(location(2,1), "int[]"),
                              new ArgNameNode(location(2,7), "x"))),
            parse(ARGS_START + "\nint[] x;\n"  + ARGS_END));
    }

    public void testWhitespace() throws Exception
    {
        assertEquals(
            argsNode()
            .addArg(new ArgNode(
                location(2,1),
                new ArgTypeNode(location(2,1), "a.b[]"),
                new ArgNameNode(location(3,1), "foo")))
            .addArg(new OptionalArgNode(
                location(4,1),
                new ArgTypeNode(location(4, 1), "a.b"),
                new ArgNameNode(location(5, 1), "x"),
                new ArgValueNode(location(6, 1), "3 "))),
            parse(ARGS_START + "\na . b [ ]\nfoo ;\na . b\nx =>\n3 ;" + ARGS_END));
    }

    public void testBadArray() throws Exception
    {
        assertError(
            ARGS_START + "\nint[ x;" + ARGS_END,
            2, 6, AbstractParser.INCOMPLETE_ARRAY_SPECIFIER_ERROR);
    }

    public void testMissingName() throws Exception
    {
        assertError(ARGS_START + "\nf;" + ARGS_END, 2, 2,
                AbstractParser.NOT_AN_IDENTIFIER_ERROR);
    }

    public void testMissingSemiAfterName() throws Exception
    {
        assertError(ARGS_START + "\nf a" + ARGS_END,
                    2, 4, OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
    }

    public void testMissingSemiAfterValue() throws Exception
    {
        assertError(ARGS_START + "\nf a => c" + ARGS_END,
                    2, 8, ArgsParser.EOF_LOOKING_FOR_SEMI);
    }

    public void testBadArgCloseTag() throws Exception
    {
        assertError(
            ARGS_START + "\n<foo",
            2,
            1,
            AbstractBodyParser.BAD_ARGS_CLOSE_TAG);
    }

    public void testEofAfterArgStart() throws Exception
    {
        assertErrorTripple(
            ARGS_START,
            1, ARGS_START.length() + 1,
            AbstractBodyParser.BAD_JAVA_TYPE_SPECIFIER,
            1, ARGS_START.length() + 1,
            AbstractBodyParser.NOT_AN_IDENTIFIER_ERROR,
            1, ARGS_START.length() + 1,
            OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);

    }

    public void testEofLookingForName() throws Exception
    {
        assertErrorPair(ARGS_START + "\na",
                        2, 2, AbstractBodyParser.NOT_AN_IDENTIFIER_ERROR,
                        2, 2, OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
    }

    public void testEofLookingForPostNameSemi() throws Exception
    {
        assertError(ARGS_START + "\na b",
                    2, 4, OptionalValueTagEndDetector.NEED_SEMI_OR_ARROW);
    }

    public void testEofLookingForValue() throws Exception
    {
        assertError(ARGS_START + "\na b =>\n",
                    3, 1, ArgsParser.EOF_LOOKING_FOR_SEMI);
    }

    public void testEofLookingForPostValueSemi() throws Exception
    {
        assertError(ARGS_START + "\na b =>\nc", 3, 1, ArgsParser.EOF_LOOKING_FOR_SEMI);
    }

    public ArgsParserTest(String p_name)
    {
        super(p_name);
    }
}
