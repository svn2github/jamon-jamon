package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.node.AbstractNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgNode;
import org.jamon.node.ArgTypeNode;
import org.jamon.node.FragmentArgsNode;

public class FragmentArgsParserTest extends AbstractParserTest
{
    private static String FRAG_END = "</%frag>";

    protected AbstractNode parse(String p_text) throws IOException
    {
        final PositionalPushbackReader reader = makeReader(p_text);
        ParserErrors errors = new ParserErrors();
        try
        {
            FragmentArgsNode result = new FragmentArgsParser(
                reader, errors, START_LOC).getFragmentArgsNode();
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


    private static FragmentArgsNode fragmentArgsNode(String p_name)
    {
        return new FragmentArgsNode(START_LOC, p_name);
    }

    public void testNoArgs() throws Exception
    {
        assertEquals(fragmentArgsNode("foo"), parse(" foo>"+ FRAG_END));
    }

    public void testNoArgsConcise() throws Exception
    {
        assertEquals(fragmentArgsNode("foo"), parse(" foo/>"));
    }
    public void testRequiredArgs() throws Exception
    {
        assertEquals(
            fragmentArgsNode("foo")
                .addArg(
                    new ArgNode(
                        location(2, 1),
                        new ArgTypeNode(location(2, 1), "int"),
                        new ArgNameNode(location(2, 5), "i")))
                .addArg(
                    new ArgNode(
                        location(3, 1),
                        new ArgTypeNode(location(3, 1), "String"),
                        new ArgNameNode(location(3, 8), "s"))),
            parse(" foo>\nint i;\nString s;\n" + FRAG_END));
    }
    public void testOptionalArgs() throws Exception

    {
        assertError(" foo>\nint i => 3;" + FRAG_END,
                    2, 7, FragmentArgsParser.NEED_SEMI);
    }

    public void testMissingLabel() throws Exception
    {
        assertError(">" + FRAG_END, 1,1, FragmentArgsParser.FRAGMENT_ARGUMENT_HAS_NO_NAME);
    }
    
    public void testMissingLabelConcise() throws Exception
    {
        assertError("/>", 1,1, FragmentArgsParser.FRAGMENT_ARGUMENT_HAS_NO_NAME);
    }
    
    public void testMissingName() throws Exception
    {
        assertError(" foo>"+ "\nf;" + FRAG_END, 2, 2,
                AbstractParser.NOT_AN_IDENTIFIER_ERROR);
    }
 
    public void testMissingSemiAfterName() throws Exception
    {
        assertError(" foo>\nf a" + FRAG_END,
                    2, 4, FragmentArgsParser.NEED_SEMI);
    }

    public void testBadArgCloseTag() throws Exception
    {
        assertError(
            " foo>\n<bar",
            2,
            1,
            AbstractBodyParser.BAD_ARGS_CLOSE_TAG);
    }

    public void testEofAfterArgStart() throws Exception
    {
        assertErrorTripple(
            " foo>\n",
            2, 1, AbstractBodyParser.BAD_JAVA_TYPE_SPECIFIER,
            2, 1, AbstractBodyParser.NOT_AN_IDENTIFIER_ERROR,
            2, 1, FragmentArgsParser.NEED_SEMI);
    }
    
    public void testEofLookingForName() throws Exception
    {
        assertErrorPair(" foo>\na",
                        2, 2, AbstractBodyParser.NOT_AN_IDENTIFIER_ERROR,
                        2, 2, FragmentArgsParser.NEED_SEMI);
    }
    
    public void testEofLookingForPostNameSemi() throws Exception
    {
        assertError(" foo>\na b", 2, 4, FragmentArgsParser.NEED_SEMI);
    }

    public FragmentArgsParserTest(String p_name)
    {
        super(p_name);
    }

}
