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
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.tests.codegen;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.jamon.codegen.IndentingWriter;

public class IndentingWriterTest
    extends TestCase
{
    private StringWriter m_stringWriter;
    private IndentingWriter m_indentingWriter;

    public void setUp() throws Exception
    {
        m_stringWriter = new StringWriter();
        m_indentingWriter = new IndentingWriter(m_stringWriter);
    }

    public void testIndentation() throws Exception
    {
        m_indentingWriter.println("line1");
        m_indentingWriter.openBlock();
        m_indentingWriter.println("line3");
        m_indentingWriter.openBlock();
        m_indentingWriter.println("line5");
        m_indentingWriter.indent(3);
        m_indentingWriter.println("line6");
        m_indentingWriter.outdent(3);
        m_indentingWriter.closeBlock();
        m_indentingWriter.closeBlock("suffix");
        m_indentingWriter.println("line9");
        String nl = System.getProperty("line.separator");
        checkOutput("line1" + nl
                    + "{" + nl + "  line3" + nl + "  {" + nl
                    + "    line5" + nl + "       line6" + nl + "  }" + nl
                    + "}suffix" + nl
                    + "line9" + nl);
    }

    public void testFinishIndentCheck()
    {
        m_indentingWriter.openBlock();
        try
        {
            m_indentingWriter.finish();
            fail("no exception thrown");
        }
        catch(IllegalStateException e) {}
        m_indentingWriter.closeBlock();
        m_indentingWriter.finish();
    }

    public void testFinishListCheck()
    {
        m_indentingWriter.openList();
        boolean exceptionThrown = false;
        try
        {
            m_indentingWriter.finish();
            fail("no exception thrown");
        }
        catch(IllegalStateException e) {}
        m_indentingWriter.closeList();
        m_indentingWriter.finish();
    }

    public void testClosingUnopenedList()
    {
        try
        {
            m_indentingWriter.closeList();
            fail("no exception thrown");
        }
        catch(IllegalStateException e) {}
    }

    public void testNoArgList()
    {
        m_indentingWriter.openList();
        m_indentingWriter.closeList();
        checkOutput("()");
    }

    public void testOneArgList()
    {
        m_indentingWriter.openList();
        m_indentingWriter.printArg("foo");
        m_indentingWriter.closeList();
        checkOutput("(foo)");
    }

    public void testTwoArgList()
    {
        m_indentingWriter.openList();
        m_indentingWriter.printArg("foo");
        m_indentingWriter.printArg("bar");
        m_indentingWriter.closeList();
        checkOutput("(foo, bar)");
    }

    public void testThreeArgList()
    {
        m_indentingWriter.openList();
        m_indentingWriter.printArg("foo");
        m_indentingWriter.printArg("bar");
        m_indentingWriter.printArg("baz");
        m_indentingWriter.closeList();
        checkOutput("(foo, bar, baz)");
    }

    private void checkOutput(String p_expected)
    {
        assertEquals(p_expected, m_stringWriter.toString());
    }
}
