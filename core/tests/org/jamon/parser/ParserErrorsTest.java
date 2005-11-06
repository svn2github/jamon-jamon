package org.jamon.parser;

public class ParserErrorsTest extends AbstractParserTest
{
    public ParserErrorsTest(String p_name)
    {
        super(p_name);
    }

    public void testMaformedJavaTag() throws Exception
    {
        assertError("foo<%java >", 1, 4, "Malformed tag");
    }

    public void testMalformedLiteralTag() throws Exception
    {
        assertError("<%LITERAL >", 1, 1, "Malformed tag");
    }

    public void testMalformedExtendsTag() throws Exception
    {
        assertError("<%extends>",
                    1, 1, TopLevelParser.MALFORMED_EXTENDS_TAG_ERROR);
        assertError("<%extends /foo f>",
                    1, 15, TopLevelParser.MALFORMED_TAG_ERROR);
    }

    public void testUnfinishedJavaTag() throws Exception
    {
        assertError(
            "foo<%java>ab</%java",
            1,
            4,
            "Reached end of file while looking for '</%java>'");
    }

    public void testUnfinishedLiteralTag() throws Exception
    {
        assertError(
            "<%LITERAL>",
            1,
            1,
            "Reached end of file while looking for '</%LITERAL>'");
    }

    public void testUnfinishedClassTag() throws Exception
    {
        assertError(
            "<%class>",
            1,
            1,
            "Reached end of file while looking for '</%class>'");
    }

    public void testCloseOnEscapeInString() throws Exception
    {
        assertError(
            "<%java> \"\\",
            1,
            9,
            "Reached end of file while inside a java quote");
    }

    public void testMalformedCloseTag() throws Exception
    {
        assertError("</%foo", 1, 1, "Malformed tag");
        assertError("</%foo >", 1, 1, "Malformed tag");
        assertError(
            "<%def foo></%def >",
            1,
            "<%def foo> ".length(),
            "Malformed tag");
    }

    public void testWrongCloseTag() throws Exception
    {
        assertError(
            "<%def bob></%foo>",
            1,
            "<%def bob> ".length(),
            "Unexpected tag close </%foo>");
    }

    public void testTopLevelCloseTag() throws Exception
    {
        assertError("</%def>", 1, 1, "Unexpected tag close </%def>");
    }

    public void testUnexpectedCloseTags() throws Exception
    {
        assertError(
            "</&>",
            1,
            1,
            AbstractBodyParser.UNEXPECTED_FRAGMENTS_CLOSE_ERROR);
        assertError(
            "</|>",
            1,
            1,
            AbstractBodyParser.UNEXPECTED_NAMED_FRAGMENT_CLOSE_ERROR);
    }

    public void testNoDefCloseTag() throws Exception
    {
        assertError("<%def a>abc", 1, 9, SubcomponentParser.makeError("def"));
    }

    public void testNoMethodCloseTag() throws Exception
    {
        assertError("<%method a>abc", 1, 12,
                    SubcomponentParser.makeError("method"));
    }

    public void testNoOverrideCloseTag() throws Exception
    {
        assertError("<%override a>abc", 1, 14,
                    SubcomponentParser.makeError("override"));
    }

    public void testNestedDefs() throws Exception
    {
        assertError(
            "<%def foo><%def bar></%def>",
            1,
            11,
            "<%def> sections only allowed at the top level of a document");
    }

    public void testNestedMethods() throws Exception
    {
        assertError(
            "<%def foo><%method bar></%def>",
            1,
            11,
            "<%method> sections only allowed at the top level of a document");
    }

    public void testNestedOverrides() throws Exception
    {
        assertError(
            "<%method foo><%override bar></%method>",
            1,
            14,
            "<%override> sections only allowed at the top level of a document");
    }

    public void testNestedAbsMethods() throws Exception
    {
        assertError(
            "<%method foo><%absmeth bar></%method>",
            1,
            14,
            "<%absmeth> sections only allowed at the top level of a document");
    }

    public void testContentInAbsMethod() throws Exception
    {
        assertError(
            "<%absmeth foo>bar",
            1, 14, TopLevelParser.BAD_ABSMETH_CONTENT);
    }

    public void testUnknownTag() throws Exception
    {
        assertError("<%foo>", 1, 1, "Unknown tag <%foo>");
        assertError("<%foo", 1, 1, "Malformed tag");
    }

    public void testEmitErrors() throws Exception
    {
        assertError(
            "<% foo #. %>",
            1,
            9,
            AbstractBodyParser.EMIT_ESCAPE_CODE_ERROR);
        assertError("<% foo", 1, 1, AbstractBodyParser.EMIT_EOF_ERROR);
        assertError(
            "<% foo #aa %>",
            1,
            9,
            AbstractBodyParser.EMIT_MISSING_TAG_END_ERROR);
        assertError("<% \". %>", 1, 4, AbstractParser.EOF_IN_JAVA_QUOTE_ERROR);
    }

    public void testClassInSubcomponent() throws Exception
    {
        assertError(
            "<%def d><%class></%def>",
            1, 9, AbstractBodyParser.CLASS_TAG_IN_SUBCOMPONENT);
    }

    public void testExtendsInSubcomponent() throws Exception
    {
        assertError(
            "<%def d><%extends foo></%def>",
            1, 9, AbstractBodyParser.EXTENDS_TAG_IN_SUBCOMPONENT);
    }

    public void testImplementsInSubcomponent() throws Exception
    {
        assertError(
            "<%def foo>\n<%implements></%def>",
            2, 1, AbstractBodyParser.IMPLEMENTS_TAG_IN_SUBCOMPONENT);
    }

    public void testParentArgsInSubcomponent() throws Exception
    {
        assertError(
            "<%def foo>\n<%xargs></%def>",
            2, 1, AbstractBodyParser.PARENT_ARGS_TAG_IN_SUBCOMPONENT);
    }

    public void testEscapeTagInSubcomponent() throws Exception
    {
        assertError(
            "<%def foo>\n<%escape #u></%def>",
            2, 1, AbstractBodyParser.ESCAPE_TAG_IN_SUBCOMPONENT);
    }

    public void testGenericTagInSubcomponent() throws Exception
    {
        assertError(
            "<%def foo>\n<%generic></%def>",
            2, 1, AbstractBodyParser.GENERIC_TAG_IN_SUBCOMPONENT);
    }

    public void testImplementMissingSemi() throws Exception
    {
        assertError("<%implements>\nfoo.bar\n</%implements>",
                    3, 1, TopLevelParser.EXPECTING_SEMI);
    }

    public void testMalformedImplementsOpen() throws Exception
    {
        assertError(
            "<%implements foo>",
            1, 1, AbstractBodyParser.MALFORMED_TAG_ERROR);
    }

    public void testMalformedImplementsClose() throws Exception
    {
        assertError(
            "<%implements>\n<foo",
            2, 1, TopLevelParser.EXPECTING_IMPLEMENTS_CLOSE);
    }

    public void testMalformedImportsOpen() throws Exception
    {
        assertError(
            "<%import foo>",
            1, 1, AbstractBodyParser.MALFORMED_TAG_ERROR);
    }

    public void testMalformedImportsClose() throws Exception
    {
        assertError(
            "<%import>\n<foo",
            2, 1, TopLevelParser.EXPECTING_IMPORTS_CLOSE);
    }

    public void testMalformedParentArgsClose() throws Exception
    {
        assertError("<%xargs>\n</%>", 2, 1, ParentArgsParser.MALFORMED_PARENT_ARGS_CLOSE);
    }

    public void testMalformedWhileTag() throws Exception
    {
        assertError("<%while>", 1, 1, AbstractBodyParser.MALFORMED_WHILE_TAG);
    }

    public void testMalformedWhileCondition() throws Exception
    {
        assertError("<%while foo>", 1, 1,
                    "Reached end of file while reading <%while ...%> tag");
    }
}
