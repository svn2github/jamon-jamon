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

import java.io.IOException;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractNode;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class AbstractClassNameParserTest extends AbstractParserTest
{
    @Test public void testParseSimple() throws Exception
    {
        assertEquals("foo", parseTypeName("foo "));
    }

    @Test public void testParseCompound() throws Exception
    {
        assertEquals("foo.bar", parseTypeName("foo. bar"));
    }

    @Test public void testParameterized() throws Exception
    {
        assertEquals("foo<Integer>", parseTypeName("foo < Integer >"));
    }

    @Test public void testParameterizedWithWildcard() throws Exception
    {
        assertEquals("foo<?>", parseTypeName("foo < ? >"));
    }

    @Test public void testParameterizedWithBoundWildcard() throws Exception
    {
        assertEquals("foo<? extends a.b>",
                     parseTypeName("foo < ? extends a . b >"));
        assertEquals("foo<? super a.b<c>>",
                     parseTypeName("foo < ? super a.b< c > >"));
    }

    @Test public void testMultipleParameters() throws Exception
    {
        assertEquals("foo.bar<T,S>", parseTypeName("foo . bar < T , S >"));
    }

    @Test public void testComplexParameters() throws Exception
    {
        String complexType =
            "foo<a,b>.bar<? extends baz.bar<T[]>,a.b<x>,? super c.d<S>>";
        assertEquals(complexType, parseTypeName(complexType));
    }

    @Test public void testParameterizingDottedName() throws Exception
    {
        assertError("foo<bar.baz extends flap>",
                    1, 1, AbstractParser.BAD_JAVA_TYPE_SPECIFIER);
    }

    /**
     * Provided for use with the AssertError method
     */
    @Override
    protected AbstractNode parse(String p_content) throws IOException
    {
        parseTypeName(p_content);
        return null;
    }

    protected abstract ClassNameParser makeParser(
        org.jamon.api.Location p_location,
        PositionalPushbackReader p_reader,
        ParserErrorsImpl p_errors) throws IOException, ParserErrorImpl;

    protected String parseTypeName(String p_content) throws IOException
    {
        ParserErrorsImpl errors = new ParserErrorsImpl();
        String result = null;
        try
        {
            ClassNameParser parser =
                makeParser(START_LOC, makeReader(p_content), errors);
            result = parser.getType();
        }
        catch (ParserErrorImpl e)
        {
            errors.addError(e);
        }
        if (errors.hasErrors())
        {
            throw errors;
        }
        return result;
    }
}
