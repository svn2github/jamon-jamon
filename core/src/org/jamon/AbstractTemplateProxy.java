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
        void writeTo(Writer p_writer);
        void escapeWith(Escaping p_escaping);
        void initialize() throws IOException;
        void autoFlush(boolean p_autoflush);
        String getPath();
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

    protected AbstractTemplateProxy(TemplateManager p_templateManager)
    {
        m_templateManager = p_templateManager;
    }

    protected final TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }

    private Escaping m_escaping;
    private final TemplateManager m_templateManager;
    private final ThreadLocal m_instance = new ThreadLocal();

    protected final void escape(Escaping p_escaping)
    {
        m_escaping = p_escaping;
    }

    protected abstract String getPath();

    protected final Intf getUntypedInstance()
        throws IOException
    {
        Intf instance = (Intf) m_instance.get();
        if (instance == null)
        {
            instance = (Intf) getTemplateManager().acquireInstance(getPath());
            instance.initialize();
            m_instance.set(instance);
        }
        if (m_escaping != null)
        {
            instance.escapeWith(m_escaping);
        }
        return instance;
    }

    protected final void releaseInstance()
        throws IOException
    {
        Intf instance = (Intf) m_instance.get();
        getTemplateManager().releaseInstance(instance);
        m_instance.set(null);
    }
}
