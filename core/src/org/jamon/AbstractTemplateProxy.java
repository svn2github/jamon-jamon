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
 * Contributor(s):
 */

package org.jamon;

import java.io.Writer;
import java.io.IOException;

import org.jamon.escaping.Escaping;

public abstract class AbstractTemplateProxy
{
    public interface Intf
    {
        void escapeWith(Escaping p_escaping);
    }

    protected static class ImplData
    {
        public final void setWriter(Writer p_writer)
        {
            m_writer = p_writer;
        }

        public final Writer getWriter()
        {
            return m_writer;
        }

        public final void setAutoFlush(boolean p_autoFlush)
        {
            m_autoFlush = p_autoFlush;
        }

        public final boolean getAutoFlush()
        {
            return m_autoFlush;
        }

        private Writer m_writer;
        private boolean m_autoFlush = true;
    }

    public static void setTemplateManagerSourceClassName(String p_className)
    {
        s_templateManagerSourceName = p_className;
    }

    private static String s_templateManagerSourceName =
        System.getProperty("org.jamon.TemplateManagerSource",
                           org.jamon.DefaultTemplateManagerSource.class.getName());

    protected AbstractTemplateProxy()
    {
        TemplateManagerSource source = null;
        try
        {
            Class tmsClass = getClass().getClassLoader()
                .loadClass(s_templateManagerSourceName);
            source = (TemplateManagerSource) tmsClass.newInstance();
            m_templateManager = source.getTemplateManager();
            m_implDataInstance = null;
        }
        catch (ClassNotFoundException e)
        {
            throw new JamonRuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new JamonRuntimeException(e);
        }
        catch (InstantiationException e)
        {
            throw new JamonRuntimeException(e);
        }
    }

    protected AbstractTemplateProxy(TemplateManager p_templateManager,
                                    boolean p_singleThreaded)
    {
        m_templateManager = p_templateManager;
        m_implDataInstance = p_singleThreaded ? makeImplData() : null;
    }

    protected final TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }

    private Escaping m_escaping = Escaping.DEFAULT;
    private final TemplateManager m_templateManager;
    private final ThreadLocal m_implData = new ThreadLocal();
    private final ImplData m_implDataInstance;

    protected final void escape(Escaping p_escaping)
    {
        m_escaping = p_escaping;
    }

    protected final Escaping getEscaping()
    {
        return m_escaping;
    }

    protected abstract AbstractTemplateImpl constructImpl(
        Class p_class, TemplateManager p_manager)
        throws IOException;

    protected abstract AbstractTemplateImpl constructImpl(
        TemplateManager p_manager)
        throws IOException;

    protected abstract ImplData makeImplData();

    protected final ImplData getImplData()
    {
        if (m_implDataInstance != null)
        {
            return m_implDataInstance;
        }
        else
        {
            ImplData implData = (ImplData) m_implData.get();
            if (implData == null)
            {
                implData = makeImplData();
                m_implData.set(implData);
            }
            return implData;
        }
    }

    public final void reset()
    {
        m_implData.set(null);
    }
}
