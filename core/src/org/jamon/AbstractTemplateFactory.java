package org.modusponens.jtt;

import java.io.Writer;

public abstract class AbstractTemplateFactory
{
    private static TemplateManager s_templateManager =
        new StandardTemplateManager();

    private TemplateManager getTemplateManager()
    {
        return s_templateManager;
    }

    protected Template getInstance(String p_path, Writer p_writer)
    {
        return getTemplateManager().getInstance(p_path, p_writer);
    }
}
