package org.jamon.parser;

public class ErrorRecoveryTest extends AbstractParserTest
{
    public ErrorRecoveryTest(String p_name)
    {
        super(p_name);
    }

    public void testCloseTagRecovery() throws Exception
    {
        assertErrorPair(
            "</%foo></%bar>",
            1,
            1,
            "Unexpected tag close </%foo>",
            1,
            8,
            "Unexpected tag close </%bar>");
        assertErrorPair(
            "<%def bob></%foo></%def>",
            1,
            11,
            "Unexpected tag close </%foo>",
            1,
            18,
            "Unexpected tag close </%def>");
    }
}
