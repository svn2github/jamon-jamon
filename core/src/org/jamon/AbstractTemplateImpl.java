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
 * Contributor(s):
 */

package org.jamon;

import java.io.Writer;
import java.io.IOException;
import java.net.URLEncoder;

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

    public final void encoding(Encoding p_encoding)
    {
        m_encoding = p_encoding;
    }

    public final void initialize()
        throws JamonException
    {
        try
        {
            initializeDefaultArguments();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (JamonException e)
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

    protected void writeEscaped(String p_string)
        throws IOException
    {
        if (getEncoding().equals(Encoding.HTML))
        {
            writeHtmlEscaped(p_string);
        }
        else if (getEncoding().equals(Encoding.NONE))
        {
            writeUnEscaped(p_string);
        }
        else if (getEncoding().equals(Encoding.XML))
        {
            writeXmlEscaped(p_string);
        }
        else if (getEncoding().equals(Encoding.URL))
        {
            writeUrlEscaped(p_string);
        }
        else
        {
            throw new JamonException("Encoding " + getEncoding() + " is not supported");
        }
    }

    protected TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }

    protected Writer getWriter()
    {
        return m_writer;
    }

    protected Encoding getEncoding()
    {
        return m_encoding;
    }

    protected void writeHtmlEscaped(String p_string)
        throws IOException
    {
        for (int i = 0;i < p_string.length(); ++i)
        {
            char c = p_string.charAt(i);
            switch (c)
            {
              case '<': m_writer.write("&lt;"); break;
              case '>': m_writer.write("&gt;"); break;
              case '&': m_writer.write("&amp;"); break;
                // The reason '"' is not escaped to "&quot;" is that it was withdrawn
                // from the HTML 3.2 DTD (only).  There does not seem to be universal 
                // agreement as to why this happened.
              case '"': m_writer.write("&#34;"); break;
              case '\'': m_writer.write("&#39;"); break;
                // FIXME: numerically escape other chars
              default: m_writer.write(c);
            }
        }
    }

    protected void writeXmlEscaped(String p_string)
        throws IOException
    {
        for (int i = 0;i < p_string.length(); ++i)
        {
            char c = p_string.charAt(i);
            switch (c)
            {
              case '<': m_writer.write("&lt;"); break;
              case '>': m_writer.write("&gt;"); break;
              case '&': m_writer.write("&amp;"); break;
              case '"': m_writer.write("&quot;"); break;
              case '\'': m_writer.write("&apos;"); break;
                // FIXME: numerically escape other chars
              default: m_writer.write(c);
            }
        }
    }

    protected void writeUnEscaped(String p_string)
        throws IOException
    {
        m_writer.write(p_string);
    }

    protected void writeUrlEscaped(String p_string)
        throws IOException
    {
        m_writer.write(URLEncoder.encode(p_string));
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
    private Encoding m_encoding = Encoding.HTML;
    private final TemplateManager m_templateManager;
    private final String m_path;
}
