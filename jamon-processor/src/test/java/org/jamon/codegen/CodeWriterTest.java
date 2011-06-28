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
 * Contributor(s): Jay Sachs
 */

package org.jamon.codegen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class CodeWriterTest
    extends TestCase
{
    private ByteArrayOutputStream m_bytes;
    private CodeWriter m_codeWriter;

    @Override public void setUp() throws Exception
    {
        m_bytes = new ByteArrayOutputStream();
        m_codeWriter = new CodeWriter(m_bytes, "US-ASCII");
    }

    public void testIndentation() throws Exception
    {
        m_codeWriter.println("line1");
        m_codeWriter.openBlock();
        m_codeWriter.println("line3");
        m_codeWriter.openBlock();
        m_codeWriter.println("line5");
        m_codeWriter.indent();
        m_codeWriter.println("line6");
        m_codeWriter.outdent();
        m_codeWriter.closeBlock();
        m_codeWriter.closeBlock("suffix");
        m_codeWriter.println("line9");
        String nl = System.getProperty("line.separator");
        checkOutput("line1" + nl
                    + "{" + nl + "  line3" + nl + "  {" + nl
                    + "    line5" + nl + "      line6" + nl + "  }" + nl
                    + "}suffix" + nl
                    + "line9" + nl);
    }

    public void testFinishIndentCheck()
        throws IOException
    {
        m_codeWriter.openBlock();
        try
        {
            m_codeWriter.finish();
            fail("no exception thrown");
        }
        catch(IllegalStateException e) {}
        m_codeWriter.closeBlock();
        m_codeWriter.finish();
    }

    public void testFinishListCheck()
        throws IOException
    {
        m_codeWriter.openList();
        try
        {
            m_codeWriter.finish();
            fail("no exception thrown");
        }
        catch(IllegalStateException e) {}
        m_codeWriter.closeList();
        m_codeWriter.finish();
    }

    public void testClosingUnopenedList()
    {
        try
        {
            m_codeWriter.closeList();
            fail("no exception thrown");
        }
        catch(IllegalStateException e) {}
    }

    public void testPrintArgOutsideOfList()
    {
        try
        {
            m_codeWriter.printListElement("foo");
            fail("no exception thrown");
        }
        catch(IllegalStateException e) {}
    }

    public void testNoArgList()
        throws IOException
    {
        m_codeWriter.openList();
        m_codeWriter.closeList();
        checkOutput("()");
    }

    public void testOneArgList()
        throws IOException
    {
        m_codeWriter.openList();
        m_codeWriter.printListElement("foo");
        m_codeWriter.closeList();
        checkOutput("(foo)");
    }

    public void testTwoArgList()
        throws IOException
    {
        m_codeWriter.openList();
        m_codeWriter.printListElement("foo");
        m_codeWriter.printListElement("bar");
        m_codeWriter.closeList();
        checkOutput("(foo, bar)");
    }

    public void testThreeArgList()
        throws IOException
    {
        m_codeWriter.openList();
        m_codeWriter.printListElement("foo");
        m_codeWriter.printListElement("bar");
        m_codeWriter.printListElement("baz");
        m_codeWriter.closeList();
        checkOutput("(foo, bar, baz)");
    }

    public void testNestedList() throws Exception
    {
        m_codeWriter.openList();
        m_codeWriter.printListElement("outer1=");
        m_codeWriter.openList("{", true);
        m_codeWriter.closeList("}");
        m_codeWriter.printListElement("outer2=");
        m_codeWriter.openList("{", true);
        m_codeWriter.printListElement("mid1=");
        m_codeWriter.openList("[", false);
        m_codeWriter.printListElement("inner1");
        m_codeWriter.printListElement("inner2");
        m_codeWriter.closeList("]");
        m_codeWriter.printListElement("mid2");
        m_codeWriter.closeList("}");
        m_codeWriter.printListElement("outer3");
        m_codeWriter.closeList();
        checkOutput("(outer1={}, outer2={\n  mid1=[inner1, inner2],\n  mid2}, outer3)");
    }

    private void checkOutput(String p_expected)
        throws IOException
    {
        m_codeWriter.finish();
        assertEquals(p_expected, new String(m_bytes.toByteArray(), "US-ASCII"));
    }
}
