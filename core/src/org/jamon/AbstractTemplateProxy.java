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
 * The Original Code is Jamon code, released ??.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon;

import java.io.Writer;

public abstract class AbstractTemplateProxy
{
    public interface Intf
    {
        void setWriter(Writer p_writer);
    }

    protected AbstractTemplateProxy(TemplateManager p_templateManager)
    {
        m_templateManager = p_templateManager;
    }

    protected final TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }

    private final TemplateManager m_templateManager;


    protected final Intf getInstance(String p_path)
        throws JamonException
    {
        if (m_instance == null)
        {
            m_instance = (Intf) getTemplateManager().getInstance(p_path);
            ((AbstractTemplateImpl)m_instance).initialize();
        }
        return m_instance;
    }

    protected final void releaseInstance()
        throws JamonException
    {
        getTemplateManager().releaseInstance((AbstractTemplateImpl)m_instance);
        m_instance = null;
    }

    private Intf m_instance;

    protected void finalize()
        throws JamonException
    {
        releaseInstance();
    }

}
