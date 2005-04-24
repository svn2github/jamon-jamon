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
import org.jamon.node.Location;;

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
    public CodeWriter(Writer p_writer)
    {
        m_writer = new PrintWriter(p_writer);
    }

    private final PrintWriter m_writer;
    private int m_indentation = 0;
    private boolean m_inList;
    private boolean m_argAlreadyPrinted;
    private boolean beginingOfLine = true;
    private static final int BASIC_OFFSET = 2;
    private static final String SPACES =
        "                                        "; // 40 spaces


    public void printLocation(Location p_location)
    {
        println("// " + p_location.getLine() + ", " + p_location.getColumn());
    }

    public void println()
    {
        println("");
    }

    public void println(Object p_obj)
    {
        maybeIndent();
        m_writer.println(p_obj);
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
        indent(BASIC_OFFSET);
    }

    public void closeBlock(String p_extra)
    {
        outdent(BASIC_OFFSET);
        println("}" + p_extra);
    }

    public void closeBlock()
    {
        closeBlock("");
    }

    public void indent(int p_spaces)
    {
        m_indentation += p_spaces;
    }

    public void outdent(int p_spaces)
    {
        m_indentation -= p_spaces;
        if (m_indentation < 0)
        {
            throw new IllegalStateException("Attempting to outdent past 0");
        }
    }

    public void openList()
    {
        if(m_inList)
        {
            throw new IllegalStateException("Nested lists not supported");
        }
        m_inList = true;
        m_argAlreadyPrinted = false;
        print("(");
    }

    public void closeList()
    {
        if(! m_inList)
        {
            throw new IllegalStateException("Attempt to close unopened list");
        }
        m_inList = false;
        print(")");
    }

    public void printArg(Object p_arg)
    {
        if(! m_inList)
        {
            throw new IllegalStateException(
                "Attempt to print arg outside of list");
        }
        if(m_argAlreadyPrinted)
        {
            print(", ");
        }
        m_argAlreadyPrinted = true;
        print(p_arg);
    }


    public void finish() throws IOException
    {
        if(m_indentation != 0)
        {
            throw new IllegalStateException("indentation is " + m_indentation
                                            + " at end of file");
        }
        if(m_inList)
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
