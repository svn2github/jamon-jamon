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
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Luis O'Shea
 */

package org.jamon;

import java.io.Writer;
import java.io.IOException;
import java.net.URLEncoder;

import org.jamon.escaping.Escaping;

public abstract class AbstractTemplateImpl
{
    protected AbstractTemplateImpl(TemplateManager p_templateManager,
                                   String p_path)
    {
        m_templateManager = p_templateManager;
        m_path = p_path;
    }

    public final String getPath()
    {
        return m_path;
    }

    public final void writeTo(Writer p_writer)
    {
        m_writer = p_writer;
    }

    public void escaping(Escaping p_escaping)
    {
        m_escaping = p_escaping;
    }

    public final void initialize()
        throws IOException
    {
        try
        {
            initializeDefaultArguments();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JamonException(e);
        }
    }

    protected void initializeDefaultArguments()
        throws Exception
    {
        // override me
    }

    protected void write(String p_string)
        throws IOException
    {
        m_writer.write(p_string);
    }

    protected void writeEscaped(String p_string)
        throws IOException
    {
        writeEscaped(p_string, m_escaping);
    }

    protected void writeEscaped(String p_string, Escaping p_escaping)
        throws IOException
    {
        p_escaping.write(p_string, m_writer);
    }

    protected TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }

    protected Writer getWriter()
    {
        return m_writer;
    }

    protected Escaping getEscaping()
    {
        return m_escaping;
    }

    protected String valueOf(Object p_obj)
    {
        return p_obj != null ? p_obj.toString() : "";
    }

    protected String valueOf(int p_int)
    {
        return String.valueOf(p_int);
    }

    protected String valueOf(double p_double)
    {
        return String.valueOf(p_double);
    }

    protected String valueOf(char p_char)
    {
        return String.valueOf(p_char);
    }

    protected String valueOf(boolean p_bool)
    {
        return String.valueOf(p_bool);
    }

    private Writer m_writer;
    private Escaping m_escaping = Escaping.DEFAULT;
    private final TemplateManager m_templateManager;
    private final String m_path;
}
