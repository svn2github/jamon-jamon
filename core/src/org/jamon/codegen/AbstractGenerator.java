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
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;

public abstract class AbstractGenerator
{
    protected AbstractGenerator(Writer p_writer,
                                TemplateResolver p_resolver)
    {
        m_writer = new IndentingWriter(p_writer);
        m_resolver = p_resolver;
    }

    protected final static String IOEXCEPTION_CLASS =
        IOException.class.getName();
    protected final static String WRITER_CLASS =
        Writer.class.getName();
    protected final static String RENDERER_CLASS =
        org.jamon.Renderer.class.getName();

    private final TemplateResolver m_resolver;
    private final IndentingWriter m_writer;

    protected TemplateResolver getResolver()
    {
        return m_resolver;
    }

    protected IndentingWriter getWriter()
    {
        return m_writer;
    }

    protected void print(Object p_obj)
    {
        m_writer.print(p_obj);
    }

    protected void println()
    {
        m_writer.println();
    }

    protected void println(Object p_obj)
    {
        m_writer.println(p_obj);
    }

    protected void openBlock()
    {
        m_writer.openBlock();
    }

    protected void closeBlock()
    {
        m_writer.closeBlock();
    }
}
