/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */
package org.jamon.parser;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.node.AbstractImportNode;
import org.jamon.node.ImportNode;
import org.jamon.node.StaticImportNode;

public class ImportParserTest extends AbstractParserTest
{
    public void testParseSimpleImport() throws Exception
    {
        assertEquals(new ImportNode(START_LOC, "foo"), parseImport("foo "));
    }

    public void testParseCompoundImport() throws Exception
    {
        assertEquals(
            new ImportNode(START_LOC, "foo.bar"), parseImport("foo . bar "));
    }

    public void testParseStarImport() throws Exception
    {
        assertEquals(
            new ImportNode(START_LOC, "foo.bar.*"), parseImport("foo.bar . *"));
    }

    public void testStaticImport() throws Exception
    {
        assertEquals(
            new StaticImportNode(START_LOC, "foo.bar"), parseImport("static foo.bar"));
    }

    public void testBadStaticImport() throws Exception
    {
        try
        {
            parseImport("static.foo.bar");
            fail("exception expected");
        }
        catch (ParserError e)
        {
           assertEquals(
               new ParserError(
                   START_LOC,
                   ImportParser.MISSING_WHITESPACE_AFTER_STATIC_DECLARATION),
               e);
        }
    }

    private AbstractImportNode parseImport(String p_content)
        throws Exception
    {
        ParserErrors errors = new ParserErrors();
        AbstractImportNode node = new ImportParser(
            START_LOC, makeReader(p_content), errors).parse().getNode();
        if (errors.hasErrors())
        {
            throw errors;
        }
        return node;
    }
}
