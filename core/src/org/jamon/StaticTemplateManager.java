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

import java.io.IOException;

import org.jamon.escaping.Escaping;
import org.jamon.util.StringUtils;

/**
 * A standard implementation of the {@link TemplateManager} interface.
 * The <code>StaticTemplateManager</code> is geared towards production
 * deployment; it is designed for performance. It will <b>NOT</b>
 * dynamically examine or recompile template sources.
 *
 * <code>StaticTemplateManager</code> instances are thread-safe.  In
 * your applications, you generally want exactly one instance of a
 * StaticTemplateManager (i.e. a singleton).
 *
 * Configuration of a <code>StaticTemplateManager</code> occurs only
 * at construction time, and is determined by the
 * <code>StaticTemplateManager.Data</code> object passed to the
 * constructor. The properties on the <code>Data</code> are:

 * <ul>

 *   <li><b>setAutoFlush</b> - determines whether templates
 *   automatically flush the writer after rendering. Default is true.

 *   <li><b>setDefaultEscaping</b> - used to set the default escaping
 *   Default is null.

 * </ul>
 */

public class StaticTemplateManager
    implements TemplateManager
{
    public static class Data
    {
        public Data setAutoFlush(boolean p_autoFlush)
        {
            autoFlush = p_autoFlush;
            return this;
        }
        private boolean autoFlush = true;

        public Data setDefaultEscaping(Escaping p_escaping)
        {
            escaping = p_escaping;
            return this;
        }
        private Escaping escaping = Escaping.DEFAULT;
    }

    public StaticTemplateManager()
    {
        this(new Data());
    }

    public StaticTemplateManager(Data p_data)
    {
        m_autoFlush = p_data.autoFlush;
        m_escaping = p_data.escaping == null
            ? Escaping.DEFAULT
            : p_data.escaping;
    }

    public AbstractTemplateProxy.Intf constructImpl(
        AbstractTemplateProxy p_proxy)
        throws IOException
    {
        return constructImpl(p_proxy, this);
    }

    /**
     * Provided for subclasses and composing classes. Given a template
     * proxy path, return an instance of the executable code for that
     * proxy's template.
     *
     * @param p_proxy a proxy for the template
     * @param p_manager the {@link TemplateManager} to supply to the
     * template
     *
     * @return a <code>Template</code> instance
     *
     * @exception IOException if something goes wrong
     **/
    public AbstractTemplateProxy.Intf constructImpl(
        AbstractTemplateProxy p_proxy, TemplateManager p_manager)
        throws IOException
    {
        return p_proxy.constructImpl(p_manager);
    }

    /**
     * Given a template path, return a proxy for that template.
     *
     * @param p_path the path to the template
     *
     * @return a <code>Template</code> proxy instance
     *
     * @exception IOException if something goes wrong
     **/
    public AbstractTemplateProxy constructProxy(String p_path)
        throws IOException
    {
        try
        {
            return (AbstractTemplateProxy) getProxyClass(p_path)
                .getConstructor(new Class [] { TemplateManager.class })
                .newInstance(new Object [] { this });
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JamonException(e);
        }
    }

    private Class getProxyClass(String p_path)
        throws IOException
    {
        return getTemplateClass(p_path,
                                StringUtils.templatePathToClassName(p_path));
    }

    private Class getTemplateClass(String p_path, String p_className)
        throws IOException
    {
        try
        {
            return Class.forName(p_className);
        }
        catch (ClassNotFoundException e)
        {
            throw new JamonException(e);
        }
    }

    private final Escaping m_escaping;
    private final boolean m_autoFlush;
}
