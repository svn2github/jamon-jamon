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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import java.io.PrintWriter;
import java.io.Writer;

public class IndentingWriter
{
    public IndentingWriter(Writer p_writer)
    {
        m_writer = new PrintWriter(p_writer);
    }

    private final PrintWriter m_writer;
    private int m_indentation = 0;
    private boolean beginingOfLine = true;
    private static final int BASIC_OFFSET = 2;
    private static final String SPACES =
        "                                        "; // 40 spaces

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

    private void maybeIndent()
    {
        if (beginingOfLine)
        {
            m_writer.print
                (SPACES.substring(0,Math.min(m_indentation, SPACES.length())));
        }
    }
}
