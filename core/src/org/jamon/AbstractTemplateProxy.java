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

public abstract class AbstractTemplateProxy
{
    public interface Intf //FIXME - need this at all?
    {
    }

    public interface FragmentIntf //FIXME - need this at all?
    {
    }

    protected static class ImplData //FIXME - need this at all?
    {
    }

    protected AbstractTemplateProxy(TemplateManager p_templateManager)
    {
        m_templateManager = p_templateManager;
    }

    protected AbstractTemplateProxy(String p_path)
    {
        this(TemplateManagerSource.getTemplateManagerFor(p_path));
    }

    protected final TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }

    private final TemplateManager m_templateManager;
    private ImplData m_implData = makeImplData();

    protected abstract AbstractTemplateImpl constructImpl(
        Class p_class, TemplateManager p_manager)
        throws IOException;

    protected abstract AbstractTemplateImpl constructImpl(
        TemplateManager p_manager)
        throws IOException;

    protected abstract ImplData makeImplData();

    protected final void reset()
    {
        m_implData = null;
    }

    protected final ImplData getImplData()
    {
        if (m_implData == null)
        {
            throw new IllegalStateException("Template has been used");
        }
        return m_implData;
    }
}
