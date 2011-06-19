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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.LinkedList;


public class CodeWriter
{
    static final String JAVA_SOURCE_ENCODING = "US-ASCII";

    public CodeWriter(OutputStream p_stream)
    {
        try
        {
            m_writer = new PrintWriter(new OutputStreamWriter(p_stream, JAVA_SOURCE_ENCODING));
        }
        catch (UnsupportedEncodingException e)
        {
            // us-ascii is guaranteed to be available
            throw new RuntimeException(e);
        }
    }

    /**
     * @deprecated only use the Stream constructor
     */
    @Deprecated
    public CodeWriter(Writer p_writer)
    {
        m_writer = new PrintWriter(p_writer);
    }

    private final PrintWriter m_writer;
    private int m_indentation = 0;
    private LinkedList<Boolean> m_argAlreadyPrintedStack = new LinkedList<Boolean>();
    private LinkedList<Boolean> m_itemPerLineStack = new LinkedList<Boolean>();
    private int nextFragmentImplCounter = 0;
    private boolean beginingOfLine = true;
    private static final int BASIC_OFFSET = 2;
    private static final String SPACES =
        "                                        "; // 40 spaces


    public int nextFragmentImplCounter()
    {
        return nextFragmentImplCounter++;
    }

    public void printLocation(org.jamon.api.Location p_location)
    {
        // In some cases, (such as children of class-only templates), we have
        // no location.
        if (p_location != null)
        {
            println("// " + p_location.getLine() + ", " + p_location.getColumn());
        }
    }

    public void println()
    {
        println("");
    }

    public void println(Object p_obj)
    {
        maybeIndent();
        m_writer.println(p_obj);
        for (StackTraceElement e: Thread.currentThread().getStackTrace()) {
            if (e.getClassName() != getClass().getName() && e.getClassName().startsWith("org.jamon")) {
                maybeIndent();
                m_writer.println("// newline called from " + e.toString());
                break;
            }
        }
        beginingOfLine = true;
    }

    public void print(Object p_obj)
    {
        maybeIndent();
        m_writer.print(p_obj);
        beginingOfLine = false;
    }

    public void openBlock()
    {
        println("{");
        indent();
    }

    public void closeBlock(String p_extra)
    {
        outdent();
        println("}" + p_extra);
    }

    public void closeBlock()
    {
        closeBlock("");
    }

    public void indent()
    {
        m_indentation += BASIC_OFFSET;
    }

    public void outdent()
    {
        m_indentation -= BASIC_OFFSET;
        if (m_indentation < 0)
        {
            throw new IllegalStateException("Attempting to outdent past 0");
        }
    }

    public void openList()
    {
        openList("(", false);
    }

    public void openList(String p_openToken, boolean p_itemPerLine)
    {
        m_argAlreadyPrintedStack.addLast(Boolean.FALSE);
        m_itemPerLineStack.addLast(Boolean.valueOf(p_itemPerLine));
        print(p_openToken);
        if (p_itemPerLine)
        {
            indent();
        }
    }

    public void closeList()
    {
        closeList(")");
    }

    public void closeList(String p_closeToken)
    {
        if(m_argAlreadyPrintedStack.isEmpty())
        {
            throw new IllegalStateException("Attempt to close unopened list");
        }
        m_argAlreadyPrintedStack.removeLast();
        if(m_itemPerLineStack.removeLast())
        {
            outdent();
        }
        print(p_closeToken);
    }

    public void printListElement(String p_listElement)
    {
        if(m_argAlreadyPrintedStack.isEmpty())
        {
            throw new IllegalStateException(
                "Attempt to print arg outside of list");
        }
        if(m_argAlreadyPrintedStack.getLast())
        {
            if (m_itemPerLineStack.getLast())
            {
                println(",");
            }
            else
            {
                print(", ");
            }
        }
        else
        {
            m_argAlreadyPrintedStack.removeLast();
            m_argAlreadyPrintedStack.addLast(Boolean.TRUE);
            if (m_itemPerLineStack.getLast()) {
                println();
            }
        }
        print(p_listElement);
    }


    public void finish() throws IOException
    {
        if(m_indentation != 0)
        {
            throw new IllegalStateException("indentation is " + m_indentation
                                            + " at end of file");
        }
        if(! m_argAlreadyPrintedStack.isEmpty())
        {
            throw new IllegalStateException("in a list at end of file");
        }
        try
        {
            if (m_writer.checkError())
            {
                throw new IOException("Exception writing to stream");
            }
        }
        finally
        {
            m_writer.close();
        }
    }

    private void maybeIndent()
    {
        if (beginingOfLine)
        {
            m_writer.print
                (SPACES.substring(0,Math.min(m_indentation, SPACES.length())));
        }
    }
}
