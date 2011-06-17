package org.jamon.codegen;

import java.util.Collection;

import org.jamon.api.ParsedTemplate;
import org.jamon.api.SourceGenerator;

public class ParsedTemplateImpl implements ParsedTemplate
{
    private final TemplateDescriber m_templateDescriber;
    private final TemplateUnit m_templateUnit;

    public ParsedTemplateImpl(TemplateDescriber p_templateDescriber, TemplateUnit p_templateUnit)
    {
        m_templateDescriber = p_templateDescriber;
        m_templateUnit = p_templateUnit;
    }

    @Override
    public SourceGenerator getImplGenerator()
    {
        return new ImplGenerator(m_templateDescriber, m_templateUnit);
    }

    @Override
    public SourceGenerator getProxyGenerator()
    {
        return new ProxyGenerator(m_templateDescriber, m_templateUnit);
    }

    @Override
    public Collection<String> getTemplateDependencies()
    {
        return m_templateUnit.getTemplateDependencies();
    }


}
