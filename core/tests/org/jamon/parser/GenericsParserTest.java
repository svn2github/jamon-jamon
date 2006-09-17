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

import org.jamon.node.GenericsParamNode;
import org.jamon.node.GenericsBoundNode;
import org.jamon.node.GenericsNode;

public class GenericsParserTest extends AbstractParserTest
{
    private final static String UNEXPECTED_CLOSE =
        "Unexpected tag close </%generic>";

    public void testNoParams() throws Exception
    {
        assertErrorPair("<%generic>\n</%generic>",
                        2, 1, GenericsParser.TYPE_PARAMETER_EXPECTED_ERROR,
                        2, 1, UNEXPECTED_CLOSE);
    }

    public void testSimpleParam() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new GenericsNode(START_LOC)
                .addParam(new GenericsParamNode(location(2,1), "T"))),
            parse("<%generic>\nT</%generic>"));
    }

    public void testSimpleParams() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new GenericsNode(START_LOC)
                .addParam(new GenericsParamNode(location(2,1), "T"))
                .addParam(new GenericsParamNode(location(2,4), "S"))),
            parse("<%generic>\nT, S</%generic>"));
    }

    public void testBoundedParam() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new GenericsNode(START_LOC)
                .addParam(new GenericsParamNode(location(2,1), "T")
                    .addBound(new GenericsBoundNode(location(3,1), "Foo")))),
            parse("<%generic>\nT extends\nFoo</%generic>"));
    }

    public void testMultiplyBoundedParam() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new GenericsNode(START_LOC)
                .addParam(new GenericsParamNode(location(2,1), "T")
                    .addBound(new GenericsBoundNode(location(3,1), "Foo"))
                    .addBound(new GenericsBoundNode(location(3,5), "bar.Baz")))),
            parse("<%generic>\nT extends\nFoo&bar.Baz</%generic>"));
    }

    public void testBoundedParams() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new GenericsNode(START_LOC)
                .addParam(new GenericsParamNode(location(2,1), "T")
                    .addBound(new GenericsBoundNode(location(3,1), "Foo")))
                .addParam(new GenericsParamNode(location(4,1), "S")
                    .addBound(new GenericsBoundNode(location(5,1), "Bar"))
                    .addBound(new GenericsBoundNode(location(5,7), "Baz")))),
            parse("<%generic>\nT extends\nFoo,\nS extends\nBar & Baz</%generic>"));
    }

    public void testBadParamName() throws Exception
    {
        assertErrorPair("<%generic>\na.b\n</%generic>",
                        2, 2, GenericsParser.EXPECTING_EXTENDS_OR_GENERIC_ERROR,
                        3,1, UNEXPECTED_CLOSE);
    }

    public void testExpectingExtends() throws Exception
    {
        assertErrorPair("<%generic>\na foo\n</%generic>",
                        2,3, GenericsParser.EXPECTING_EXTENDS_OR_GENERIC_ERROR,
                        3,1, UNEXPECTED_CLOSE);
    }

    public void testBadBounds() throws Exception
    {
        assertErrorPair("<%generic>a extends\n*</%generic>",
                    2,1, AbstractParser.BAD_JAVA_TYPE_SPECIFIER,
                    2,2, UNEXPECTED_CLOSE);
    }
}
