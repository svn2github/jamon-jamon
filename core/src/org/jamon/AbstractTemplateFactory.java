package org.modusponens.jtt;

import java.io.Writer;

public abstract class AbstractTemplateFactory
{
    private final TemplateManager m_templateManager;

    private TemplateManager getTemplateManager()
    {
        return m_templateManager;
    }

    protected AbstractTemplateFactory(TemplateManager p_templateManager)
    {
        m_templateManager = p_templateManager;
    }

    protected Template getInstance(String p_path, Writer p_writer)
    {
        return getTemplateManager().getInstance(p_path, p_writer);
    }
}
